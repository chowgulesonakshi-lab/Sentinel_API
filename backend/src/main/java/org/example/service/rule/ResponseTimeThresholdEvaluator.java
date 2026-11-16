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
public class ResponseTimeThresholdEvaluator implements AlertConditionEvaluator {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(AlertRule.ConditionType type) {
        return type == AlertRule.ConditionType.RESPONSE_TIME_THRESHOLD;
    }

    @Override
    public EvaluationResult evaluate(AlertRule rule, Endpoint endpoint, Metrics currentMetric, MetricRepository metricRepository) {
        if (currentMetric.getResponseTime() == null) {
            return EvaluationResult.ok();
        }

        double responseTime = currentMetric.getResponseTime();
        double thresholdMillis = 1000.0;
        String params = rule.getConditionParameters();

        if (params != null && !params.isBlank()) {
            try {
                if (params.trim().startsWith("{")) {
                    JsonNode node = objectMapper.readTree(params);
                    if (node.has("thresholdMillis")) {
                        thresholdMillis = node.get("thresholdMillis").asDouble();
                    } else if (node.has("threshold")) {
                        thresholdMillis = node.get("threshold").asDouble();
                    }
                } else {
                    thresholdMillis = Double.parseDouble(params.trim());
                }
            } catch (Exception e) {
                log.warn("Failed to parse response time threshold parameter '{}': {}", params, e.getMessage());
            }
        }

        if (responseTime >= thresholdMillis) {
            String message = String.format("Rule '%s' triggered: Response time %.0f ms exceeded threshold of %.0f ms",
                    rule.getName(), responseTime, thresholdMillis);
            return EvaluationResult.trigger(message, Alerts.AlertType.HIGH_LATENCY);
        }

        return EvaluationResult.ok();
    }
}
