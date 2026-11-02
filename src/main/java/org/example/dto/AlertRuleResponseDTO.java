package org.example.dto;

import lombok.Data;
import org.example.entity.AlertRule;
import org.example.entity.Alerts;

import java.time.LocalDateTime;

@Data
public class AlertRuleResponseDTO {
    private Long id;
    private String name;
    private AlertRule.ConditionType conditionType;
    private String conditionParameters;
    private Alerts.Severity severity;
    private boolean enabled;

    private Long endpointId;
    private String endpointUrl;
    private String endpointMethod;

    private Long apiServiceId;
    private String apiServiceName;

    private Integer evaluationWindowMinutes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
