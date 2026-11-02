package org.example.dto;

import lombok.Data;
import org.example.entity.Metrics;

import java.time.LocalDateTime;

@Data
public class MetricsResponseDTO {
    private Long id;

    private Long endpointId;
    private String endpointUrl;

    private Metrics.HealthStatus status;
    private Integer statusCode;

    private boolean success;

    private Double responseTime;
    private LocalDateTime timeStamp;
    private boolean anomalyEvaluated;
    private boolean anomalous;
    private Integer anomalyObservationCount;
    private Double historicalAverage;
    private Double standardDeviation;
    private Double anomalyDetectionThreshold;
    private Double deviationFromAverage;
    private Double anomalySensitivityMultiplier;
    private String anomalyExplanation;
}
