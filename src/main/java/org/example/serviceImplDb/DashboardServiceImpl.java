package org.example.serviceImplDb;

import lombok.RequiredArgsConstructor;
import org.example.dto.ApiHealthDTO;
import org.example.dto.DashboardResponseDTO;
import org.example.dto.MetricsResponseDTO;
import org.example.entity.Alerts;
import org.example.entity.Endpoint;
import org.example.entity.Metrics;
import org.example.repository.*;
import org.example.service.DashboardService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private final ProjectRepository projectRepository;
    private final ApiServiceRepository apiServiceRepository;
    private final EndpointRepository endpointRepository;
    private final MetricRepository metricRepository;
    private final AlertsRepository alertsRepository;


    @Override
    public DashboardResponseDTO getDashboardSummary() {
        DashboardResponseDTO response = new DashboardResponseDTO();
        response.setTotalProjects(projectRepository.findAll().stream()
                .filter(org.example.security.ResourceOwnership::isCurrentUser).count());
        response.setTotalApiServices(apiServiceRepository.findAll().stream()
                .filter(org.example.security.ResourceOwnership::isCurrentUser).count());
        response.setTotalEndpoints(endpointRepository.findAll().stream()
                .filter(org.example.security.ResourceOwnership::isCurrentUser).count());

        List<Metrics> ownedMetrics = metricRepository.findAll().stream()
                .filter(org.example.security.ResourceOwnership::isCurrentUser).toList();
        Map<Long, Metrics> latestByEndpoint = ownedMetrics.stream()
                .filter(m -> m.getEndpoint() != null)
                .collect(Collectors.toMap(m -> m.getEndpoint().getId(), m -> m,
                        (a, b) -> a.getTimeStamp().isAfter(b.getTimeStamp()) ? a : b));
        response.setSuccessfulChecks(latestByEndpoint.values().stream()
                .filter(m -> m.getStatus() == Metrics.HealthStatus.UP).count());
        response.setFailedChecks(latestByEndpoint.values().stream()
                .filter(m -> m.getStatus() == Metrics.HealthStatus.DOWN).count());
        double avg = ownedMetrics.stream().filter(Metrics::isSuccess)
                .map(Metrics::getResponseTime).filter(java.util.Objects::nonNull)
                .mapToDouble(Double::doubleValue).average().orElse(0.0);
        response.setAverageResponseTime(avg);
        response.setLastHealthCheck(ownedMetrics.stream().map(Metrics::getTimeStamp)
                .filter(java.util.Objects::nonNull).max(LocalDateTime::compareTo).orElse(null));
        List<Alerts> activeAlerts = alertsRepository.findByResolvedFalse().stream()
                .filter(org.example.security.ResourceOwnership::isCurrentUser).toList();
        response.setAlerts(activeAlerts);
        response.setActiveAlerts(activeAlerts.size());

        return response;
    }

    @Override
    public List<MetricsResponseDTO> getRecentMetrics() {
        List<Metrics> metrics = metricRepository.findAll().stream()
                .filter(org.example.security.ResourceOwnership::isCurrentUser)
                .sorted(Comparator.comparing(Metrics::getTimeStamp, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10).toList();
        List<MetricsResponseDTO> dtoList = new ArrayList<>();
        for (Metrics metric : metrics) {
            MetricsResponseDTO dto = new MetricsResponseDTO();

            dto.setId(metric.getId());
            dto.setEndpointId(metric.getEndpoint().getId());
            dto.setEndpointUrl(metric.getEndpoint().getUrl());

            dto.setStatus(metric.getStatus());
            dto.setStatusCode(metric.getStatusCode());
            dto.setSuccess(metric.isSuccess());

            dto.setResponseTime(metric.getResponseTime());
            dto.setTimeStamp(metric.getTimeStamp());

            dtoList.add(dto);
        }
        return dtoList;
    }

    @Override
    public List<ApiHealthDTO> getApiHealth() {
        List<Endpoint> endpoints = endpointRepository.findAll().stream()
                .filter(org.example.security.ResourceOwnership::isCurrentUser).toList();
        return endpoints.stream().map(endpoint -> {
            ApiHealthDTO dto = new ApiHealthDTO();
            dto.setId(endpoint.getId());
            dto.setUrl(endpoint.getUrl());
            dto.setName(endpoint.getApiService().getName());

            Metrics metrics = metricRepository.findTopByEndpoint_IdOrderByTimeStampDesc(endpoint.getId())
                    .orElse(null);
            if(metrics!=null){
                dto.setLatency(metrics.getResponseTime());
                dto.setStatusCode(metrics.getStatusCode());
                if(metrics.isSuccess()){
                    if(metrics.getResponseTime() > 1000) {
                        dto.setStatus("SLOW");
                    } else {
                        dto.setStatus("HEALTHY");
                    }
                } else{
                    dto.setStatus("DOWN");
                }
            } else{
                dto.setStatus("UNKNOWN");
            }
            return dto;
        }).toList();
    }
}
