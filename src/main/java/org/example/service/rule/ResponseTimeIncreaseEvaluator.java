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

import java.util.List;

@Component
@Slf4j
public class ResponseTimeIncreaseEvaluator implements AlertConditionEvaluator {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(AlertRule.ConditionType type) {
        return type == AlertRule.ConditionType.RESPONSE_TIME_INCREASE;
    }

    @Override
    public EvaluationResult evaluate(AlertRule rule, Endpoint endpoint, Metrics currentMetric, MetricRepository metricRepository) {
        if (currentMetric.getResponseTime() == null || !currentMetric.isSuccess()) {
            return EvaluationResult.ok();
        }

        double percentIncrease = 50.0;
        String params = rule.getConditionParameters();
        if (params != null && !params.isBlank()) {
            try {
                if (params.trim().startsWith("{")) {
                    JsonNode node = objectMapper.readTree(params);
                    if (node.has("percent")) {
                        percentIncrease = node.get("percent").asDouble();
                    } else if (node.has("percentage")) {
                        percentIncrease = node.get("percentage").asDouble();
                    }
                } else {
                    percentIncrease = Double.parseDouble(params.trim());
                }
            } catch (Exception e) {
                log.warn("Failed to parse response time increase parameter '{}': {}", params, e.getMessage());
            }
        }

        List<Metrics> recentMetrics = metricRepository.findTop10ByEndpointIdOrderByTimeStampDesc(endpoint.getId());
        // Calculate baseline excluding the current metric
        List<Metrics> historical = recentMetrics.stream()
                .filter(m -> !m.getId().equals(currentMetric.getId()) && m.isSuccess() && m.getResponseTime() != null)
                .toList();

        if (historical.size() < 3) {
            // Need at least 3 historical data points to establish a reliable baseline
            return EvaluationResult.ok();
        }

        double baselineAvg = historical.stream()
                .mapToDouble(Metrics::getResponseTime)
                .average()
                .orElse(0.0);

        if (baselineAvg > 0) {
            double actualIncreasePercent = ((currentMetric.getResponseTime() - baselineAvg) / baselineAvg) * 100.0;
            if (actualIncreasePercent >= percentIncrease) {
                String message = String.format("Rule '%s' triggered: Response time %.0f ms is %.1f%% higher than baseline (%.0f ms)",
                        rule.getName(), currentMetric.getResponseTime(), actualIncreasePercent, baselineAvg);
                return EvaluationResult.trigger(message, Alerts.AlertType.HIGH_LATENCY);
            }
        }

        return EvaluationResult.ok();
    }
}
