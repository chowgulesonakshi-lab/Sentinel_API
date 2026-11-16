package org.example.service.rule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.AlertRule;
import org.example.entity.Alerts;
import org.example.entity.Endpoint;
import org.example.entity.Metrics;
import org.example.repository.AlertRuleRepository;
import org.example.repository.AlertsRepository;
import org.example.repository.MetricRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlertRuleEngine {

    private final List<AlertConditionEvaluator> evaluators;
    private final AlertRuleRepository alertRuleRepository;
    private final AlertsRepository alertsRepository;
    private final MetricRepository metricRepository;

    @Transactional
    public void evaluateRulesForMetric(Endpoint endpoint, Metrics currentMetric) {
        Long serviceId = endpoint.getApiService() != null ? endpoint.getApiService().getId() : null;
        List<AlertRule> applicableRules = alertRuleRepository.findApplicableRules(endpoint.getId(), serviceId);

        if (applicableRules.isEmpty()) {
            return;
        }

        for (AlertRule rule : applicableRules) {
            try {
                evaluateRule(rule, endpoint, currentMetric);
            } catch (Exception e) {
                log.error("Error evaluating alert rule '{}' (id: {}): {}", rule.getName(), rule.getId(), e.getMessage());
            }
        }
    }

    private void evaluateRule(AlertRule rule, Endpoint endpoint, Metrics currentMetric) {
        AlertConditionEvaluator evaluator = evaluators.stream()
                .filter(ev -> ev.supports(rule.getConditionType()))
                .findFirst()
                .orElse(null);

        if (evaluator == null) {
            log.warn("No evaluator found for condition type: {}", rule.getConditionType());
            return;
        }

        EvaluationResult result = evaluator.evaluate(rule, endpoint, currentMetric, metricRepository);

        Optional<Alerts> activeAlertOpt = alertsRepository.findByEndpointIdAndRuleIdAndResolvedFalse(endpoint.getId(), rule.getId());

        if (result.isTriggered()) {
            // Prevent duplicate alert generation if an active alert already exists for this rule and endpoint
            if (activeAlertOpt.isPresent()) {
                log.debug("Active alert already exists for rule '{}' on endpoint {}", rule.getName(), endpoint.getUrl());
            } else {
                Alerts newAlert = new Alerts();
                newAlert.setEndpoint(endpoint);
                newAlert.setRule(rule);
                newAlert.setSeverity(rule.getSeverity());
                newAlert.setType(result.getAlertType() != null ? result.getAlertType() : Alerts.AlertType.CUSTOM_RULE);
                newAlert.setMessage(result.getMessage());
                newAlert.setResolved(false);
                newAlert.setCreatedAt(LocalDateTime.now());

                alertsRepository.save(newAlert);
                log.info("Triggered new alert for rule '{}' [{}]: {}", rule.getName(), rule.getSeverity(), result.getMessage());
            }
        } else {
            // If condition is no longer met and metric is successful, resolve the active incident
            if (currentMetric.isSuccess() && activeAlertOpt.isPresent()) {
                Alerts activeAlert = activeAlertOpt.get();
                activeAlert.setResolved(true);
                activeAlert.setResolvedAt(LocalDateTime.now());
                alertsRepository.save(activeAlert);
                log.info("Auto-resolved alert #{} for rule '{}' on endpoint {}", activeAlert.getId(), rule.getName(), endpoint.getUrl());
            }
        }
    }
}
