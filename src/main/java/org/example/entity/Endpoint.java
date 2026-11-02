package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "endpoints")
public class Endpoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private HttpMethod method;

    private String url;
    private Integer timeoutMillis;
    private boolean enabled;

    @Column(name = "interval_seconds")
    private Integer intervalSeconds;

    @Column(name = "expected_status_codes")
    private String expectedStatusCodes;

    @Column(name = "slow_response_threshold_millis")
    private Integer slowResponseThresholdMillis;

    @Column(name = "consecutive_failures_threshold")
    private Integer consecutiveFailuresThreshold;

    @Column(name = "consecutive_failures_count")
    private Integer consecutiveFailuresCount;

    @Column(name = "anomaly_detection_enabled")
    private Boolean anomalyDetectionEnabled;

    @Column(name = "anomaly_sensitivity_multiplier")
    private Double anomalySensitivityMultiplier;

    @Column(name = "anomaly_observation_window")
    private Integer anomalyObservationWindow;

    @Column(name = "anomaly_minimum_observations")
    private Integer anomalyMinimumObservations;

    @Column(columnDefinition = "TEXT")
    private String headers;

    @Column(name = "request_body", columnDefinition = "TEXT")
    private String requestBody;

    @Column(name = "last_monitored_at")
    private java.time.LocalDateTime lastMonitoredAt;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private ApiService apiService;

    @PrePersist
    @PreUpdate
    public void ensureDefaults() {
        if (intervalSeconds == null || intervalSeconds < 5) {
            intervalSeconds = 60;
        }
        if (timeoutMillis == null || timeoutMillis < 100) {
            timeoutMillis = 5000;
        }
        if (expectedStatusCodes == null || expectedStatusCodes.trim().isEmpty()) {
            expectedStatusCodes = "200,201,202,204";
        }
        if (slowResponseThresholdMillis == null || slowResponseThresholdMillis <= 0) {
            slowResponseThresholdMillis = 1000;
        }
        if (consecutiveFailuresThreshold == null || consecutiveFailuresThreshold < 1) {
            consecutiveFailuresThreshold = 1;
        }
        if (consecutiveFailuresCount == null) {
            consecutiveFailuresCount = 0;
        }
        if (anomalySensitivityMultiplier == null || anomalySensitivityMultiplier <= 0) {
            anomalySensitivityMultiplier = 3.0;
        }
        if (anomalyDetectionEnabled == null) {
            anomalyDetectionEnabled = true;
        }
        if (anomalyObservationWindow == null || anomalyObservationWindow < 1) {
            anomalyObservationWindow = 20;
        }
        if (anomalyMinimumObservations == null || anomalyMinimumObservations < 3) {
            anomalyMinimumObservations = 5;
        }
    }

    public enum HttpMethod {
        GET,
        POST,
        PUT,
        DELETE,
        PATCH
    }
}
