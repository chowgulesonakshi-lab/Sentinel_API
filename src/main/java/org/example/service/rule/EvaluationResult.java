package org.example.service.rule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.entity.Alerts;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationResult {
    private boolean triggered;
    private String message;
    private Alerts.AlertType alertType;

    public static EvaluationResult ok() {
        return new EvaluationResult(false, null, null);
    }

    public static EvaluationResult trigger(String message, Alerts.AlertType type) {
        return new EvaluationResult(true, message, type);
    }
}
