package org.example.serviceImplDb;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.Endpoint;
import org.example.entity.Metrics;
import org.example.repository.EndpointRepository;
import org.example.repository.MetricRepository;
import org.example.service.MonitoringService;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MonitoringServiceImpl implements MonitoringService {
    private final EndpointRepository endpointRepository;
    private final MetricRepository metricRepository;
    RestTemplate restTemplate = new RestTemplate();

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
        long startTime = System.currentTimeMillis();
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint.getUrl(),
                    HttpMethod.valueOf(endpoint.getMethod().name()),
                    null,
                    String.class);

            long endTime = System.currentTimeMillis();

            metrics.setEndpoint(endpoint);
            metrics.setResponseTime((double) (endTime - startTime));
            metrics.setStatusCode(response.getStatusCode().value());
            metrics.setSuccess(true);
            metrics.setStatus(Metrics.HealthStatus.UP);
            metrics.setTimeStamp(LocalDateTime.now());

            log.info("Endpoint {} is UP | Status={} | Response Time={} ms",
                    endpoint.getUrl(),
                    response.getStatusCode().value(),
                    metrics.getResponseTime());

        } catch (HttpStatusCodeException e) {
            long endTime = System.currentTimeMillis();

            metrics.setEndpoint(endpoint);
            metrics.setResponseTime((double) (endTime - startTime));
            metrics.setStatusCode(e.getStatusCode().value());
            metrics.setSuccess(false);
            metrics.setStatus(Metrics.HealthStatus.DOWN);
            metrics.setTimeStamp(LocalDateTime.now());

            log.warn("Endpoint {} returned HTTP {}",
                    endpoint.getUrl(),
                    e.getStatusCode().value());
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();

            metrics.setEndpoint(endpoint);
            metrics.setResponseTime((double) (endTime - startTime));
            metrics.setStatusCode(500); // or 0 if you want to indicate no HTTP response
            metrics.setSuccess(false);
            metrics.setStatus(Metrics.HealthStatus.DOWN);
            metrics.setTimeStamp(LocalDateTime.now());

            log.error("Failed to monitor endpoint {}: {}",
                    endpoint.getUrl(),
                    e.getMessage());
        }
        metricRepository.save(metrics);
    }
}
