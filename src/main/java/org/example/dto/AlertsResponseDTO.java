package org.example.dto;

import lombok.Data;
import org.example.entity.Alerts;

import java.time.LocalDateTime;

@Data
public class AlertsResponseDTO {
    private Long id;
    private Long endpointId;
    private String endpointUrl;
    private Alerts.AlertType type;
    private Alerts.Severity severity;
    private String message;
    private boolean resolved;
    private Long ruleId;
    private String ruleName;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private Double anomalyObservedResponseTime;
    private Double anomalyHistoricalAverage;
    private Double anomalyStandardDeviation;
    private Double anomalyDetectionThreshold;
    private Double anomalyDeviationFromAverage;
    private Double anomalySensitivityMultiplier;
    private String anomalyExplanation;
}
