package org.example.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.entity.AlertRule;
import org.example.entity.Alerts;

@Data
public class AlertRuleRequestDTO {

    @NotBlank(message = "Rule name is required")
    private String name;

    @NotNull(message = "Condition type is required")
    private AlertRule.ConditionType conditionType;

    private String conditionParameters;

    @NotNull(message = "Severity is required")
    private Alerts.Severity severity;

    private boolean enabled = true;

    private Long endpointId;
    private Long apiServiceId;

    @Min(value = 1, message = "Evaluation window must be at least 1 minute")
    private Integer evaluationWindowMinutes = 15;
}
