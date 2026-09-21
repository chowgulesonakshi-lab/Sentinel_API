package org.example.serviceImplDb;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.Alerts;
import org.example.entity.Endpoint;
import org.example.entity.Metrics;
import org.example.repository.EndpointRepository;
import org.example.service.AlertsService;
import org.example.service.MetricsService;
import org.example.service.MonitoringService;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MonitoringServiceImpl implements MonitoringService {
    private final EndpointRepository endpointRepository;
    private final MetricsService metricsService;
    private final AlertsService alertsService;
    private final RestTemplate restTemplate = new RestTemplate();;

    @Override
    public void monitorAllEndpoints() {
        List<Endpoint> endpoints = endpointRepository.findByEnabledTrue();
        log.info("Monitoring {} enabled endpoints", endpoints.size());
        for (Endpoint endpoint : endpoints) {
            monitorEndpoint(endpoint);
        }
        log.info("Monitoring cycle completed");
    }

    @Override
    public void monitorEndpoint(Endpoint endpoint) {
        Metrics metrics = new Metrics();
        metrics.setEndpoint(endpoint);
        long startTime = System.currentTimeMillis();
        try {
            int timeout = endpoint.getTimeoutMillis() > 0 ? endpoint.getTimeoutMillis() : 5000;
            org.springframework.http.client.SimpleClientHttpRequestFactory requestFactory =
                    new org.springframework.http.client.SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(timeout);
            requestFactory.setReadTimeout(timeout);
            RestTemplate timeoutRestTemplate = new RestTemplate(requestFactory);

            ResponseEntity<String> response = timeoutRestTemplate.exchange(
                    endpoint.getUrl(),
                    HttpMethod.valueOf(endpoint.getMethod().name()),
                    null,
                    String.class
            );
            metrics.setStatusCode(response.getStatusCode().value());
            metrics.setSuccess(true);
            metrics.setStatus(Metrics.HealthStatus.UP);
            log.info(
                    "Endpoint {} is UP | Status={} | Response Time={} ms",
                    endpoint.getUrl(),
                    response.getStatusCode().value(),
                    System.currentTimeMillis() - startTime
            );
        } catch (HttpStatusCodeException e) {
            metrics.setStatusCode(e.getStatusCode().value());
            metrics.setSuccess(false);
            metrics.setStatus(Metrics.HealthStatus.DOWN);
            log.warn(
                    "Endpoint {} returned HTTP {}",
                    endpoint.getUrl(),
                    e.getStatusCode().value()
            );
        } catch (Exception e) {
            metrics.setStatusCode(500);
            metrics.setSuccess(false);
            metrics.setStatus(Metrics.HealthStatus.DOWN);
            log.error(
                    "Failed to monitor endpoint {}: {}",
                    endpoint.getUrl(),
                    e.getMessage()
            );
        } finally {
            metrics.setResponseTime((double) (System.currentTimeMillis() - startTime));
            metrics.setTimeStamp(LocalDateTime.now());
            metricsService.saveMetric(metrics);
            if (!metrics.isSuccess()) {
                createAlert(
                        endpoint,
                        Alerts.AlertType.ENDPOINT_DOWN,
                        Alerts.Severity.CRITICAL,
                        "Endpoint is down"
                );
            } else {
                // Resolve previous DOWN alerts
                resolveAlert(endpoint.getId());

                // Create slow response warning
                if (metrics.getResponseTime() > endpoint.getTimeoutMillis()) {
                    createAlert(
                            endpoint,
                            Alerts.AlertType.SLOW_RESPONSE,
                            Alerts.Severity.WARNING,
                            "Response time exceeded threshold"
                    );
                }
            }
        }
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