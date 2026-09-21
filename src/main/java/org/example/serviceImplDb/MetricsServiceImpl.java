package org.example.serviceImplDb;

import lombok.RequiredArgsConstructor;
import org.example.entity.Metrics;
import org.example.repository.MetricRepository;
import org.example.service.MetricsService;
import org.springframework.stereotype.Service;
import org.example.dto.MetricsResponseDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MetricsServiceImpl implements MetricsService {
    private final MetricRepository metricRepository;
    private MetricsResponseDTO mapToDTO(Metrics metrics) {

        MetricsResponseDTO dto = new MetricsResponseDTO();

        dto.setId(metrics.getId());
        dto.setResponseTime(metrics.getResponseTime());
        dto.setSuccess(metrics.isSuccess());
        dto.setStatus(metrics.getStatus());
        dto.setStatusCode(metrics.getStatusCode());
        dto.setTimeStamp(metrics.getTimeStamp());

        if (metrics.getEndpoint() != null) {
            dto.setEndpointId(metrics.getEndpoint().getId());
            dto.setEndpointUrl(metrics.getEndpoint().getUrl());
        }

        return dto;
    }

    @Override
    public Metrics saveMetric(Metrics metric) {
        return metricRepository.save(metric);
    }

    @Override
    public List<MetricsResponseDTO> getAllMetrics() {
        return metricRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public Optional<MetricsResponseDTO> getMetricById(Long id) {
        return metricRepository.findById(id)
                .map(this::mapToDTO);
    }

    @Override
    public List<MetricsResponseDTO> getMetricsByEndpoint(Long endpointId) {
        return metricRepository.findByEndpointId(endpointId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<MetricsResponseDTO> getSlowMetrics(double responseTime) {
        return metricRepository.findByResponseTimeGreaterThan(responseTime)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<MetricsResponseDTO> getMetricsByStatusCode(int statusCode) {
        return metricRepository.findByStatusCode(statusCode)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<MetricsResponseDTO> getLatestMetrics() {
        return metricRepository.findTop10ByOrderByTimeStampDesc()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public long getUpCount() {
        return metricRepository.countLatestByStatus(Metrics.HealthStatus.UP);
    }

    @Override
    public long getDownCount() {
        return metricRepository.countLatestByStatus(Metrics.HealthStatus.DOWN);
    }

    @Override
    public long getFailedChecksCount() {
        return metricRepository.countBySuccessFalse();
    }

    @Override
    public Double getAverageResponseTime() {
        return metricRepository.getAverageResponseTime();
    }

    @Override
    public LocalDateTime getLastHealthCheck() {
        return metricRepository.getLastHealthCheck();
    }

    @Override
    public Optional<MetricsResponseDTO> getLatestMetricForEndpoint(Long endpointId) {
        return metricRepository.findTopByEndpoint_IdOrderByTimeStampDesc(endpointId)
                .map(this::mapToDTO);
    }

    @Override
    public long getSlowApisCount() {
        return metricRepository.countLatestSlowApis(1000);
    }
}