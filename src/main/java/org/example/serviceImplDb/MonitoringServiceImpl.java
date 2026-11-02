package org.example.serviceImplDb;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.Alerts;
import org.example.entity.Endpoint;
import org.example.entity.Metrics;
import org.example.repository.EndpointRepository;
import org.example.service.AlertsService;
import org.example.service.MetricsService;
import org.example.service.MonitoringService;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResourceAccessException;
import org.example.service.rule.AlertRuleEngine;
import org.example.service.anomaly.AnomalyDetectionResult;
import org.example.service.anomaly.AnomalyDetectionService;
import org.springframework.web.client.RestTemplate;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MonitoringServiceImpl implements MonitoringService {
    private final EndpointRepository endpointRepository;
    private final MetricsService metricsService;
    private final AlertsService alertsService;
    private final AlertRuleEngine alertRuleEngine;
    private final AnomalyDetectionService anomalyDetectionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public MonitoringServiceImpl(EndpointRepository endpointRepository,
                                 MetricsService metricsService,
                                 AlertsService alertsService,
                                 AlertRuleEngine alertRuleEngine,
                                 AnomalyDetectionService anomalyDetectionService) {
        this.endpointRepository = endpointRepository;
        this.metricsService = metricsService;
        this.alertsService = alertsService;
        this.alertRuleEngine = alertRuleEngine;
        this.anomalyDetectionService = anomalyDetectionService;
    }

    public MonitoringServiceImpl(EndpointRepository endpointRepository,
                                 MetricsService metricsService,
                                 AlertsService alertsService) {
        this(endpointRepository, metricsService, alertsService, null);
    }

    public MonitoringServiceImpl(EndpointRepository endpointRepository,
                                 MetricsService metricsService,
                                 AlertsService alertsService,
                                 AlertRuleEngine alertRuleEngine) {
        this(endpointRepository, metricsService, alertsService, alertRuleEngine, null);
    }

    @Override
    public void monitorAllEndpoints() {
        List<Endpoint> endpoints = endpointRepository.findByEnabledTrue();
        log.info("Monitoring all {} enabled endpoints", endpoints.size());
        for (Endpoint endpoint : endpoints) {
            try {
                monitorEndpoint(endpoint);
            } catch (Exception e) {
                log.error("Error monitoring endpoint {}: {}", endpoint.getUrl(), e.getMessage());
            }
        }
        log.info("Monitoring cycle completed");
    }

    @Override
    public void monitorDueEndpoints() {
        List<Endpoint> endpoints = endpointRepository.findByEnabledTrue();
        LocalDateTime now = LocalDateTime.now();

        for (Endpoint endpoint : endpoints) {
            int interval = (endpoint.getIntervalSeconds() != null && endpoint.getIntervalSeconds() > 0)
                    ? endpoint.getIntervalSeconds()
                    : 60;

            boolean isDue = endpoint.getLastMonitoredAt() == null
                    || !endpoint.getLastMonitoredAt().plusSeconds(interval).isAfter(now);

            if (isDue) {
                try {
                    monitorEndpoint(endpoint);
                } catch (Exception e) {
                    log.error("Error during scheduled check for endpoint {}: {}", endpoint.getUrl(), e.getMessage());
                }
            }
        }
    }

    @Override
    public Metrics monitorEndpointById(Long endpointId) {
        Endpoint endpoint = endpointRepository.findById(endpointId)
                .orElseThrow(() -> new RuntimeException("Endpoint not found: " + endpointId));
        return monitorEndpoint(endpoint);
    }

    @Override
    public Metrics monitorEndpoint(Endpoint endpoint) {
        Metrics metrics = new Metrics();
        metrics.setEndpoint(endpoint);

        int timeout = (endpoint.getTimeoutMillis() != null && endpoint.getTimeoutMillis() > 0)
                ? endpoint.getTimeoutMillis()
                : 5000;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeout);
        requestFactory.setReadTimeout(timeout);

        RestTemplate client = new RestTemplate(requestFactory);
        // Do not throw exceptions on 4xx/5xx so we can check expectedStatusCodes accurately
        client.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) {
                return false;
            }
        });

        HttpHeaders headers = parseHeaders(endpoint.getHeaders());
        String body = endpoint.getRequestBody();
        if (body != null && !body.isBlank() && headers.getContentType() == null) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);

        long startTime = System.currentTimeMillis();
        try {
            ResponseEntity<String> response = client.exchange(
                    endpoint.getUrl(),
                    HttpMethod.valueOf(endpoint.getMethod().name()),
                    httpEntity,
                    String.class
            );

            int statusCode = response.getStatusCode().value();
            metrics.setStatusCode(statusCode);

            boolean isSuccess = isExpectedStatusCode(statusCode, endpoint.getExpectedStatusCodes());
            metrics.setSuccess(isSuccess);
            metrics.setStatus(isSuccess ? Metrics.HealthStatus.UP : Metrics.HealthStatus.DOWN);

            log.info("Endpoint {} [{}] responded with status {} | Success: {} | Time: {} ms",
                    endpoint.getMethod(), endpoint.getUrl(), statusCode, isSuccess,
                    System.currentTimeMillis() - startTime);

        } catch (ResourceAccessException e) {
            metrics.setSuccess(false);
            metrics.setStatus(Metrics.HealthStatus.DOWN);
            if (e.getCause() instanceof SocketTimeoutException) {
                metrics.setStatusCode(504); // Gateway Timeout
                log.warn("Endpoint {} timed out after {} ms", endpoint.getUrl(), timeout);
            } else if (e.getCause() instanceof ConnectException) {
                metrics.setStatusCode(503); // Service Unavailable / connection refused
                log.warn("Endpoint {} connection refused", endpoint.getUrl());
            } else {
                metrics.setStatusCode(500);
                log.warn("Endpoint {} network error: {}", endpoint.getUrl(), e.getMessage());
            }
        } catch (Exception e) {
            metrics.setStatusCode(500);
            metrics.setSuccess(false);
            metrics.setStatus(Metrics.HealthStatus.DOWN);
            log.error("Failed to monitor endpoint {}: {}", endpoint.getUrl(), e.getMessage());
        } finally {
            metrics.setResponseTime((double) (System.currentTimeMillis() - startTime));
            metrics.setTimeStamp(LocalDateTime.now());
            metricsService.saveMetric(metrics);

            handleAlertsAndThresholds(endpoint, metrics);
        }

        return metrics;
    }

    private void handleAlertsAndThresholds(Endpoint endpoint, Metrics metrics) {
        int consecutiveFailures = endpoint.getConsecutiveFailuresCount() != null
                ? endpoint.getConsecutiveFailuresCount()
                : 0;

        int consecutiveThreshold = (endpoint.getConsecutiveFailuresThreshold() != null && endpoint.getConsecutiveFailuresThreshold() > 0)
                ? endpoint.getConsecutiveFailuresThreshold()
                : 1;

        if (!metrics.isSuccess()) {
            consecutiveFailures++;
            endpoint.setConsecutiveFailuresCount(consecutiveFailures);

            if (consecutiveFailures >= consecutiveThreshold) {
                createAlert(
                        endpoint,
                        Alerts.AlertType.ENDPOINT_DOWN,
                        Alerts.Severity.CRITICAL,
                        String.format("Endpoint failed %d consecutive check(s) with HTTP %d (Threshold: %d)",
                                consecutiveFailures, metrics.getStatusCode(), consecutiveThreshold)
                );
            } else {
                log.info("Endpoint {} failed (consecutive: {}/{}), threshold not reached yet",
                        endpoint.getUrl(), consecutiveFailures, consecutiveThreshold);
            }
        } else {
            endpoint.setConsecutiveFailuresCount(0);
            resolveAlert(endpoint.getId());

            int slowThreshold = (endpoint.getSlowResponseThresholdMillis() != null && endpoint.getSlowResponseThresholdMillis() > 0)
                    ? endpoint.getSlowResponseThresholdMillis()
                    : 1000;

            if (metrics.getResponseTime() > slowThreshold) {
                createAlert(
                        endpoint,
                        Alerts.AlertType.SLOW_RESPONSE,
                        Alerts.Severity.WARNING,
                        String.format("Response time %.0f ms exceeded configured slow threshold %d ms",
                                metrics.getResponseTime(), slowThreshold)
                );
            }
        }

        // Evaluate dynamic alert rules configured by the user
        if (alertRuleEngine != null) {
            try {
                alertRuleEngine.evaluateRulesForMetric(endpoint, metrics);
            } catch (Exception e) {
                log.error("Dynamic alert rule evaluation error for endpoint {}: {}", endpoint.getUrl(), e.getMessage());
            }
        }

        evaluateAnomaly(endpoint, metrics);

        endpoint.setLastMonitoredAt(LocalDateTime.now());
        endpointRepository.save(endpoint);
    }

    private void evaluateAnomaly(Endpoint endpoint, Metrics metrics) {
        if (anomalyDetectionService == null) {
            return;
        }
        try {
            AnomalyDetectionResult result = anomalyDetectionService.evaluate(endpoint, metrics);
            if (result.isAnomalous()) {
                if (alertsService.getActiveAlert(endpoint.getId(), Alerts.AlertType.ANOMALY_DETECTED).isEmpty()) {
                    Alerts alert = new Alerts();
                    alert.setEndpoint(endpoint);
                    alert.setType(Alerts.AlertType.ANOMALY_DETECTED);
                    alert.setSeverity(Alerts.Severity.WARNING);
                    alert.setMessage(result.getExplanation());
                    alert.setResolved(false);
                    alert.setCreatedAt(LocalDateTime.now());
                    alert.setAnomalyObservedResponseTime(result.getObservedResponseTime());
                    alert.setAnomalyHistoricalAverage(result.getHistoricalAverage());
                    alert.setAnomalyStandardDeviation(result.getStandardDeviation());
                    alert.setAnomalyDetectionThreshold(result.getDetectionThreshold());
                    alert.setAnomalyDeviationFromAverage(result.getDeviationFromAverage());
                    alert.setAnomalySensitivityMultiplier(result.getSensitivityMultiplier());
                    alert.setAnomalyExplanation(result.getExplanation());
                    alertsService.saveAlert(alert);
                }
            } else {
                alertsService.getActiveAlert(endpoint.getId(), Alerts.AlertType.ANOMALY_DETECTED)
                        .ifPresent(alert -> {
                            alert.setResolved(true);
                            alert.setResolvedAt(LocalDateTime.now());
                            alertsService.saveAlert(alert);
                        });
            }
        } catch (Exception e) {
            log.error("Anomaly evaluation error for endpoint {}: {}", endpoint.getUrl(), e.getMessage());
        }
    }

    private HttpHeaders parseHeaders(String headersStr) {
        HttpHeaders httpHeaders = new HttpHeaders();
        if (headersStr == null || headersStr.trim().isEmpty()) {
            return httpHeaders;
        }

        String trimmed = headersStr.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            try {
                Map<String, String> map = objectMapper.readValue(trimmed, new TypeReference<>() {});
                map.forEach(httpHeaders::set);
                return httpHeaders;
            } catch (Exception e) {
                log.warn("Failed to parse headers JSON: {}, fallback to line parser", e.getMessage());
            }
        }

        String[] lines = trimmed.split("[\\r\\n]+");
        for (String line : lines) {
            int colonIdx = line.indexOf(':');
            if (colonIdx > 0) {
                String key = line.substring(0, colonIdx).trim();
                String value = line.substring(colonIdx + 1).trim();
                if (!key.isEmpty() && !value.isEmpty()) {
                    httpHeaders.set(key, value);
                }
            }
        }
        return httpHeaders;
    }

    private boolean isExpectedStatusCode(int statusCode, String expectedCodes) {
        if (expectedCodes == null || expectedCodes.trim().isEmpty()) {
            return statusCode >= 200 && statusCode < 300;
        }

        String[] parts = expectedCodes.split(",");
        for (String part : parts) {
            String token = part.trim();
            if (token.isEmpty()) continue;
            if (token.contains("-")) {
                String[] range = token.split("-");
                try {
                    int start = Integer.parseInt(range[0].trim());
                    int end = Integer.parseInt(range[1].trim());
                    if (statusCode >= start && statusCode <= end) {
                        return true;
                    }
                } catch (NumberFormatException ignored) {}
            } else {
                try {
                    if (statusCode == Integer.parseInt(token)) {
                        return true;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return false;
    }

    private void createAlert(Endpoint endpoint,
                             Alerts.AlertType type,
                             Alerts.Severity severity,
                             String message) {
        if (alertsService.getActiveAlert(endpoint.getId(), type).isPresent()) {
            return;
        }
        Alerts alert = new Alerts();
        alert.setEndpoint(endpoint);
        alert.setType(type);
        alert.setSeverity(severity);
        alert.setMessage(message);
        alert.setResolved(false);
        alert.setCreatedAt(LocalDateTime.now());

        alertsService.saveAlert(alert);
    }

    private void resolveAlert(Long endpointId) {
        alertsService.getActiveAlert(endpointId, Alerts.AlertType.ENDPOINT_DOWN)
                .ifPresent(alert -> {
                    alert.setResolved(true);
                    alert.setResolvedAt(LocalDateTime.now());
                    alertsService.saveAlert(alert);
                });
        alertsService.getActiveAlert(endpointId, Alerts.AlertType.SLOW_RESPONSE)
                .ifPresent(alert -> {
                    alert.setResolved(true);
                    alert.setResolvedAt(LocalDateTime.now());
                    alertsService.saveAlert(alert);
                });
    }
}
