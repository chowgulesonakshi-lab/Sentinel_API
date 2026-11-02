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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class UnavailabilityDurationEvaluator implements AlertConditionEvaluator {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(AlertRule.ConditionType type) {
        return type == AlertRule.ConditionType.UNAVAILABILITY_DURATION;
    }

    @Override
    public EvaluationResult evaluate(AlertRule rule, Endpoint endpoint, Metrics currentMetric, MetricRepository metricRepository) {
        if (currentMetric.isSuccess()) {
            return EvaluationResult.ok();
        }

        int durationMinutes = 5;
        String params = rule.getConditionParameters();
        if (params != null && !params.isBlank()) {
            try {
                if (params.trim().startsWith("{")) {
                    JsonNode node = objectMapper.readTree(params);
                    if (node.has("durationMinutes")) {
                        durationMinutes = node.get("durationMinutes").asInt();
                    }
                } else {
                    durationMinutes = Integer.parseInt(params.trim());
                }
            } catch (Exception e) {
                log.warn("Failed to parse unavailability duration parameter '{}': {}", params, e.getMessage());
            }
        }

        List<Metrics> recent = metricRepository.findTop10ByEndpointIdOrderByTimeStampDesc(endpoint.getId());
        LocalDateTime oldestFailureInStreak = currentMetric.getTimeStamp();

        for (Metrics m : recent) {
            if (!m.isSuccess()) {
                if (m.getTimeStamp() != null && m.getTimeStamp().isBefore(oldestFailureInStreak)) {
                    oldestFailureInStreak = m.getTimeStamp();
                }
            } else {
                // Streak of failures broke
                break;
            }
        }

        if (oldestFailureInStreak != null) {
            long minutesDown = Duration.between(oldestFailureInStreak, LocalDateTime.now()).toMinutes();
            if (minutesDown >= durationMinutes) {
                String message = String.format("Rule '%s' triggered: Endpoint has been continuously unavailable for %d minute(s) (Threshold: %d min)",
                        rule.getName(), minutesDown, durationMinutes);
                return EvaluationResult.trigger(message, Alerts.AlertType.UNAVAILABLE_DURATION);
            }
        }

        return EvaluationResult.ok();
    }
}
