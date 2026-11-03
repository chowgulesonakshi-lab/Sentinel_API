package org.example.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.Alerts;
import org.example.entity.Endpoint;
import org.example.entity.Metrics;
import org.example.repository.AlertsRepository;
import org.example.repository.EndpointRepository;
import org.example.repository.MetricRepository;
import org.example.repository.ServiceDependencyRepository;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class IncidentContextBuilder {

    private final MetricRepository metricRepository;
    private final EndpointRepository endpointRepository;
    private final AlertsRepository alertsRepository;
    private final ServiceDependencyRepository serviceDependencyRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public IncidentContextBuilder(MetricRepository metricRepository,
                                  EndpointRepository endpointRepository,
                                  AlertsRepository alertsRepository) {
        this(metricRepository, endpointRepository, alertsRepository, null);
    }

    public IncidentContext buildContext(Alerts alert) {
        Endpoint endpoint = alert.getEndpoint();
        Long endpointId = endpoint != null ? endpoint.getId() : null;

        List<Metrics> recentMetrics = endpointId != null
                ? metricRepository.findTop10ByEndpointIdOrderByTimeStampDesc(endpointId)
                : Collections.emptyList();

        Metrics latestMetric = recentMetrics.isEmpty() ? null : recentMetrics.get(0);

        // Calculate historical baseline latency excluding failed checks
        List<Metrics> successfulHistorical = recentMetrics.stream()
                .filter(m -> m.isSuccess() && m.getResponseTime() != null)
                .toList();

        Double baselineAvg = null;
        if (!successfulHistorical.isEmpty()) {
            baselineAvg = successfulHistorical.stream()
                    .mapToDouble(Metrics::getResponseTime)
                    .average()
                    .orElse(0.0);
        }

        Double currentLatency = latestMetric != null ? latestMetric.getResponseTime() : null;
        Double deviationPercent = null;
        if (currentLatency != null && baselineAvg != null && baselineAvg > 0) {
            deviationPercent = ((currentLatency - baselineAvg) / baselineAvg) * 100.0;
        }

        // Status code distribution in recent checks
        Map<Integer, Long> statusDist = recentMetrics.stream()
                .filter(m -> m.getStatusCode() != null)
                .collect(Collectors.groupingBy(Metrics::getStatusCode, Collectors.counting()));

        List<String> directDependencies = new ArrayList<>();
        List<String> indirectDependencies = new ArrayList<>();
        List<String> potentiallyAffectedServices = new ArrayList<>();
        if (serviceDependencyRepository != null && endpoint != null && endpoint.getApiService() != null) {
            Long serviceId = endpoint.getApiService().getId();
            serviceDependencyRepository.findByDependentServiceId(serviceId).forEach(d ->
                    directDependencies.add(d.getDependencyService().getName()));
            Set<Long> visited = new LinkedHashSet<>();
            Deque<Long> queue = new ArrayDeque<>(serviceDependencyRepository.findByDependencyServiceId(serviceId).stream()
                    .map(d -> d.getDependentService().getId()).toList());
            while (!queue.isEmpty()) {
                Long affectedId = queue.removeFirst();
                if (!visited.add(affectedId)) continue;
                serviceDependencyRepository.findByDependencyServiceId(affectedId).forEach(d -> queue.addLast(d.getDependentService().getId()));
                serviceDependencyRepository.findByProjectId(endpoint.getApiService().getProject().getId()).stream()
                        .filter(d -> d.getDependentService().getId().equals(affectedId))
                        .findFirst()
                        .ifPresent(d -> potentiallyAffectedServices.add(d.getDependentService().getName()));
            }
            Set<String> directNames = new HashSet<>(directDependencies);
            potentiallyAffectedServices.stream().filter(name -> !directNames.contains(name)).forEach(indirectDependencies::add);
        }

        // Sibling endpoints in the same ApiService
        List<IncidentContext.SiblingEndpointSummary> siblings = new ArrayList<>();
        if (endpoint != null && endpoint.getApiService() != null) {
            List<Endpoint> serviceEndpoints = endpointRepository.findByApiServiceId(endpoint.getApiService().getId());
            for (Endpoint ep : serviceEndpoints) {
                if (!ep.getId().equals(endpointId)) {
                    Optional<Metrics> lastEpMetric = metricRepository.findTopByEndpoint_IdOrderByTimeStampDesc(ep.getId());
                    boolean hasActiveAlert = !alertsRepository.findByEndpointIdAndResolvedFalse(ep.getId()).isEmpty();

                    siblings.add(IncidentContext.SiblingEndpointSummary.builder()
                            .method(ep.getMethod() != null ? ep.getMethod().name() : "GET")
                            .url(ep.getUrl())
                            .enabled(ep.isEnabled())
                            .lastStatusCode(lastEpMetric.map(Metrics::getStatusCode).orElse(null))
                            .hasActiveAlert(hasActiveAlert)
                            .build());
                }
            }
        }

        List<IncidentContext.MetricSummary> metricSummaries = recentMetrics.stream()
                .map(m -> IncidentContext.MetricSummary.builder()
                        .timestamp(m.getTimeStamp() != null ? m.getTimeStamp().format(FORMATTER) : "N/A")
                        .responseTime(m.getResponseTime())
                        .statusCode(m.getStatusCode())
                        .success(m.isSuccess())
                        .healthStatus(m.getStatus() != null ? m.getStatus().name() : "UNKNOWN")
                        .build())
                .toList();

        return IncidentContext.builder()
                .alertId(alert.getId())
                .alertType(alert.getType() != null ? alert.getType().name() : "UNKNOWN")
                .severity(alert.getSeverity() != null ? alert.getSeverity().name() : "WARNING")
                .alertMessage(alert.getMessage())
                .alertCreatedAt(alert.getCreatedAt())
                .ruleName(alert.getRule() != null ? alert.getRule().getName() : null)
                .endpointId(endpointId)
                .endpointMethod(endpoint != null && endpoint.getMethod() != null ? endpoint.getMethod().name() : "GET")
                .endpointUrl(endpoint != null ? endpoint.getUrl() : "Unknown")
                .timeoutMillis(endpoint != null && endpoint.getTimeoutMillis() != null ? endpoint.getTimeoutMillis() : 5000)
                .intervalSeconds(endpoint != null && endpoint.getIntervalSeconds() != null ? endpoint.getIntervalSeconds() : 60)
                .expectedStatusCodes(endpoint != null && endpoint.getExpectedStatusCodes() != null ? endpoint.getExpectedStatusCodes() : "200")
                .slowResponseThresholdMillis(endpoint != null && endpoint.getSlowResponseThresholdMillis() != null ? endpoint.getSlowResponseThresholdMillis() : 1000)
                .consecutiveFailuresThreshold(endpoint != null && endpoint.getConsecutiveFailuresThreshold() != null ? endpoint.getConsecutiveFailuresThreshold() : 1)
                .consecutiveFailuresCount(endpoint != null && endpoint.getConsecutiveFailuresCount() != null ? endpoint.getConsecutiveFailuresCount() : 0)
                .serviceName(endpoint != null && endpoint.getApiService() != null ? endpoint.getApiService().getName() : "Unknown Service")
                .projectName(endpoint != null && endpoint.getApiService() != null && endpoint.getApiService().getProject() != null
                        ? endpoint.getApiService().getProject().getName() : "Unknown Project")
                .currentResponseTime(currentLatency)
                .currentStatusCode(latestMetric != null ? latestMetric.getStatusCode() : null)
                .historicalBaselineAvgResponseTime(baselineAvg)
                .latencyDeviationPercent(deviationPercent)
                .recentMetrics(metricSummaries)
                .siblingEndpoints(siblings)
                .statusCodeDistribution(statusDist)
                .directDependencies(directDependencies)
                .indirectDependencies(indirectDependencies)
                .potentiallyAffectedServices(potentiallyAffectedServices)
                .build();
    }
}
