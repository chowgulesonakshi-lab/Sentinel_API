package org.example.service.rule;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.AlertRule;
import org.example.entity.Alerts;
import org.example.entity.Endpoint;
import org.example.entity.Metrics;
import org.example.repository.MetricRepository;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Slf4j
public class StatusCodeEvaluator implements AlertConditionEvaluator {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(AlertRule.ConditionType type) {
        return type == AlertRule.ConditionType.STATUS_CODE;
    }

    @Override
    public EvaluationResult evaluate(AlertRule rule, Endpoint endpoint, Metrics currentMetric, MetricRepository metricRepository) {
        if (currentMetric.getStatusCode() == null) {
            return EvaluationResult.ok();
        }

        int statusCode = currentMetric.getStatusCode();
        String params = rule.getConditionParameters();
        if (params == null || params.isBlank()) {
            // Default: alert on any 5xx error
            if (statusCode >= 500) {
                return EvaluationResult.trigger(
                        String.format("Rule '%s' triggered: Server returned HTTP status %d", rule.getName(), statusCode),
                        Alerts.AlertType.STATUS_CODE_ERROR
                );
            }
            return EvaluationResult.ok();
        }

        try {
            if (params.trim().startsWith("{")) {
                JsonNode node = objectMapper.readTree(params);
                int targetCode = node.has("statusCode") ? node.get("statusCode").asInt() : 500;
                String op = node.has("operator") ? node.get("operator").asText() : "==";

                boolean match = switch (op) {
                    case ">=" -> statusCode >= targetCode;
                    case ">" -> statusCode > targetCode;
                    case "<=" -> statusCode <= targetCode;
                    case "<" -> statusCode < targetCode;
                    case "!=" -> statusCode != targetCode;
                    default -> statusCode == targetCode;
                };

                if (match) {
                    return EvaluationResult.trigger(
                            String.format("Rule '%s' triggered: HTTP status %d matches condition (%s %d)",
                                    rule.getName(), statusCode, op, targetCode),
                            Alerts.AlertType.STATUS_CODE_ERROR
                    );
                }
            } else {
                // Comma-separated list of status codes
                boolean inList = Arrays.stream(params.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .anyMatch(s -> {
                            try {
                                return Integer.parseInt(s) == statusCode;
                            } catch (NumberFormatException e) {
                                return false;
                            }
                        });

                if (inList) {
                    return EvaluationResult.trigger(
                            String.format("Rule '%s' triggered: HTTP status %d matched target list [%s]",
                                    rule.getName(), statusCode, params),
                            Alerts.AlertType.STATUS_CODE_ERROR
                    );
                }
            }
        } catch (Exception e) {
            log.warn("Failed to evaluate status code condition for rule '{}': {}", rule.getName(), e.getMessage());
        }

        return EvaluationResult.ok();
    }
}
