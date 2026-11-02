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

@Component
@Slf4j
public class ConsecutiveFailuresEvaluator implements AlertConditionEvaluator {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(AlertRule.ConditionType type) {
        return type == AlertRule.ConditionType.CONSECUTIVE_FAILURES;
    }

    @Override
    public EvaluationResult evaluate(AlertRule rule, Endpoint endpoint, Metrics currentMetric, MetricRepository metricRepository) {
        if (currentMetric.isSuccess()) {
            return EvaluationResult.ok();
        }

        int threshold = 3;
        String params = rule.getConditionParameters();
        if (params != null && !params.isBlank()) {
            try {
                if (params.trim().startsWith("{")) {
                    JsonNode node = objectMapper.readTree(params);
                    if (node.has("threshold")) {
                        threshold = node.get("threshold").asInt();
                    }
                } else {
                    threshold = Integer.parseInt(params.trim());
                }
            } catch (Exception e) {
                log.warn("Failed to parse consecutive failures parameter '{}': {}", params, e.getMessage());
            }
        }

        int consecutive = endpoint.getConsecutiveFailuresCount() != null ? endpoint.getConsecutiveFailuresCount() : 1;
        if (consecutive >= threshold) {
            String message = String.format("Rule '%s' triggered: Endpoint failed %d consecutive check(s) (Threshold: %d)",
                    rule.getName(), consecutive, threshold);
            return EvaluationResult.trigger(message, Alerts.AlertType.ENDPOINT_DOWN);
        }

        return EvaluationResult.ok();
    }
}
