package org.example.serviceImplDb;

import lombok.RequiredArgsConstructor;
import org.example.entity.Metrics;
import org.example.repository.MetricRepository;
import org.example.service.MetricsService;
import org.example.service.anomaly.AnomalyDetectionResult;
import org.example.service.anomaly.AnomalyDetectionService;
import org.springframework.stereotype.Service;
import org.example.dto.MetricsResponseDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MetricsServiceImpl implements MetricsService {
    private final MetricRepository metricRepository;
    private final AnomalyDetectionService anomalyDetectionService;
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
            AnomalyDetectionResult anomaly = anomalyDetectionService.evaluate(metrics.getEndpoint(), metrics);
            dto.setAnomalyEvaluated(anomaly.isEvaluated());
            dto.setAnomalous(anomaly.isAnomalous());
            dto.setAnomalyObservationCount(anomaly.getObservationCount());
            dto.setHistoricalAverage(anomaly.getHistoricalAverage());
            dto.setStandardDeviation(anomaly.getStandardDeviation());
            dto.setAnomalyDetectionThreshold(anomaly.getDetectionThreshold());
            dto.setDeviationFromAverage(anomaly.getDeviationFromAverage());
            dto.setAnomalySensitivityMultiplier(anomaly.getSensitivityMultiplier());
            dto.setAnomalyExplanation(anomaly.getExplanation());
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
                .filter(org.example.security.ResourceOwnership::isCurrentUser)
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public Optional<MetricsResponseDTO> getMetricById(Long id) {
        return metricRepository.findById(id)
                .filter(org.example.security.ResourceOwnership::isCurrentUser)
                .map(this::mapToDTO);
    }

    @Override
    public List<MetricsResponseDTO> getMetricsByEndpoint(Long endpointId) {
        return metricRepository.findByEndpointId(endpointId)
                .stream()
                .filter(org.example.security.ResourceOwnership::isCurrentUser)
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<MetricsResponseDTO> getSlowMetrics(double responseTime) {
        return metricRepository.findByResponseTimeGreaterThan(responseTime)
                .stream()
                .filter(org.example.security.ResourceOwnership::isCurrentUser)
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<MetricsResponseDTO> getMetricsByStatusCode(int statusCode) {
        return metricRepository.findByStatusCode(statusCode)
                .stream()
                .filter(org.example.security.ResourceOwnership::isCurrentUser)
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<MetricsResponseDTO> getLatestMetrics() {
        return metricRepository.findTop10ByOrderByTimeStampDesc()
                .stream()
                .filter(org.example.security.ResourceOwnership::isCurrentUser)
                .map(this::mapToDTO)
                .toList();
    }

    private List<Metrics> ownedMetrics() {
        return metricRepository.findAll().stream()
                .filter(org.example.security.ResourceOwnership::isCurrentUser)
                .toList();
    }

    private List<Metrics> latestOwnedMetrics() {
        return ownedMetrics().stream()
                .filter(metric -> metric.getEndpoint() != null)
                .collect(Collectors.toMap(metric -> metric.getEndpoint().getId(), metric -> metric,
                        (a, b) -> Comparator.nullsFirst(LocalDateTime::compareTo)
                                .compare(a.getTimeStamp(), b.getTimeStamp()) >= 0 ? a : b))
                .values().stream().toList();
    }

    @Override
    public long getUpCount() {
        return latestOwnedMetrics().stream().filter(m -> m.getStatus() == Metrics.HealthStatus.UP).count();
    }

    @Override
    public long getDownCount() {
        return latestOwnedMetrics().stream().filter(m -> m.getStatus() == Metrics.HealthStatus.DOWN).count();
    }

    @Override
    public long getFailedChecksCount() {
        return ownedMetrics().stream().filter(m -> !m.isSuccess()).count();
    }

    @Override
    public Double getAverageResponseTime() {
        return ownedMetrics().stream().filter(Metrics::isSuccess)
                .map(Metrics::getResponseTime).filter(java.util.Objects::nonNull)
                .mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    @Override
    public LocalDateTime getLastHealthCheck() {
        return ownedMetrics().stream().map(Metrics::getTimeStamp)
                .filter(java.util.Objects::nonNull).max(LocalDateTime::compareTo).orElse(null);
    }

    @Override
    public Optional<MetricsResponseDTO> getLatestMetricForEndpoint(Long endpointId) {
        return metricRepository.findTopByEndpoint_IdOrderByTimeStampDesc(endpointId)
                .filter(org.example.security.ResourceOwnership::isCurrentUser)
                .map(this::mapToDTO);
    }

    @Override
    public long getSlowApisCount() {
        return latestOwnedMetrics().stream()
                .filter(m -> m.getResponseTime() != null && m.getResponseTime() >= 1000).count();
    }
}
