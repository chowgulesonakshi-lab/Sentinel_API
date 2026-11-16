package org.example.service;

import org.example.dto.MetricsResponseDTO;
import org.example.entity.Metrics;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MetricsService {
    Metrics saveMetric(Metrics metric);

    List<MetricsResponseDTO> getAllMetrics();
    Optional<MetricsResponseDTO> getMetricById(Long id);
    List<MetricsResponseDTO> getMetricsByEndpoint(Long endpointId);
    List<MetricsResponseDTO> getSlowMetrics(double responseTime);
    List<MetricsResponseDTO> getMetricsByStatusCode(int statusCode);
    List<MetricsResponseDTO> getLatestMetrics();

    long getUpCount();
    long getDownCount();
    long getSlowApisCount();

    long getFailedChecksCount();
    Double getAverageResponseTime();
    LocalDateTime getLastHealthCheck();
    Optional<MetricsResponseDTO> getLatestMetricForEndpoint(Long endpointId);
}
