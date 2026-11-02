package org.example.service.rule;

import org.example.entity.AlertRule;
import org.example.entity.Endpoint;
import org.example.entity.Metrics;
import org.example.repository.MetricRepository;

public interface AlertConditionEvaluator {
    boolean supports(AlertRule.ConditionType type);
    EvaluationResult evaluate(AlertRule rule, Endpoint endpoint, Metrics currentMetric, MetricRepository metricRepository);
}
