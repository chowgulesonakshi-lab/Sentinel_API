package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "alerts")
public class Alerts {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "endpointId")
    private Endpoint endpoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id")
    private AlertRule rule;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    private AlertType type;

    private String message;
    private boolean resolved;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    @Column(name = "anomaly_observed_response_time")
    private Double anomalyObservedResponseTime;

    @Column(name = "anomaly_historical_average")
    private Double anomalyHistoricalAverage;

    @Column(name = "anomaly_standard_deviation")
    private Double anomalyStandardDeviation;

    @Column(name = "anomaly_detection_threshold")
    private Double anomalyDetectionThreshold;

    @Column(name = "anomaly_deviation_from_average")
    private Double anomalyDeviationFromAverage;

    @Column(name = "anomaly_sensitivity_multiplier")
    private Double anomalySensitivityMultiplier;

    @Column(name = "anomaly_explanation", columnDefinition = "TEXT")
    private String anomalyExplanation;

    public enum AlertType {
        ENDPOINT_DOWN,
        SLOW_RESPONSE,
        CUSTOM_RULE,
        STATUS_CODE_ERROR,
        HIGH_LATENCY,
        UNAVAILABLE_DURATION,
        ANOMALY_DETECTED
    }

    public enum Severity {
        INFO,
        WARNING,
        CRITICAL
    }
}
