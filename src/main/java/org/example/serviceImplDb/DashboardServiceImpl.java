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
        response.setTotalProjects(projectRepository.count());
        response.setTotalApiServices(apiServiceRepository.count());
        response.setTotalEndpoints(endpointRepository.count());

        response.setSuccessfulChecks(metricRepository.countLatestByStatus(Metrics.HealthStatus.UP));
        response.setFailedChecks(metricRepository.countLatestByStatus(Metrics.HealthStatus.DOWN));

        Double avg = metricRepository.getAverageResponseTime();
        response.setAverageResponseTime(avg!=null ? avg:0.0);

        response.setLastHealthCheck(metricRepository.getLastHealthCheck());
        List<Alerts> activeAlerts =
                alertsRepository.findByResolvedFalse();
        response.setAlerts(activeAlerts);
        response.setActiveAlerts(activeAlerts.size());

        return response;
    }

    @Override
    public List<MetricsResponseDTO> getRecentMetrics() {
        List<Metrics> metrics = metricRepository.findTop10ByOrderByTimeStampDesc();
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
        List<Endpoint> endpoints = endpointRepository.findAll();
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
