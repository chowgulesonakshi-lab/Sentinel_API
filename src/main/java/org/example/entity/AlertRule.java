package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "alert_rules")
public class AlertRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false)
    private ConditionType conditionType;

    @Column(name = "condition_parameters", columnDefinition = "TEXT")
    private String conditionParameters;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Alerts.Severity severity;

    private boolean enabled;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endpoint_id")
    private Endpoint endpoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private ApiService apiService;

    @Column(name = "evaluation_window_minutes")
    private Integer evaluationWindowMinutes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.evaluationWindowMinutes == null || this.evaluationWindowMinutes <= 0) {
            this.evaluationWindowMinutes = 15;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum ConditionType {
        CONSECUTIVE_FAILURES,
        STATUS_CODE,
        RESPONSE_TIME_THRESHOLD,
        RESPONSE_TIME_INCREASE,
        UNAVAILABILITY_DURATION
    }
}
