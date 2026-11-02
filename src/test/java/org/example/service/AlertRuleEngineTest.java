package org.example.service;

import org.example.entity.AlertRule;
import org.example.entity.Alerts;
import org.example.entity.Endpoint;
import org.example.entity.Metrics;
import org.example.repository.AlertRuleRepository;
import org.example.repository.AlertsRepository;
import org.example.repository.MetricRepository;
import org.example.service.rule.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AlertRuleEngineTest {

    @Mock
    private AlertRuleRepository alertRuleRepository;

    @Mock
    private AlertsRepository alertsRepository;

    @Mock
    private MetricRepository metricRepository;

    private AlertRuleEngine alertRuleEngine;

    @BeforeEach
    void setUp() {
        List<AlertConditionEvaluator> evaluators = List.of(
                new ConsecutiveFailuresEvaluator(),
                new StatusCodeEvaluator(),
                new ResponseTimeThresholdEvaluator(),
                new ResponseTimeIncreaseEvaluator(),
                new UnavailabilityDurationEvaluator()
        );
        alertRuleEngine = new AlertRuleEngine(evaluators, alertRuleRepository, alertsRepository, metricRepository);
    }

    @Test
    void testStatusCodeRuleTriggerAndCreation() {
        Endpoint endpoint = new Endpoint();
        endpoint.setId(1L);

        Metrics metric = new Metrics();
        metric.setId(100L);
        metric.setStatusCode(503);
        metric.setSuccess(false);

        AlertRule rule = new AlertRule();
        rule.setId(10L);
        rule.setName("Server 5xx Errors");
        rule.setConditionType(AlertRule.ConditionType.STATUS_CODE);
        rule.setConditionParameters("{\"statusCode\": 500, \"operator\": \">=\"}");
        rule.setSeverity(Alerts.Severity.CRITICAL);
        rule.setEnabled(true);

        when(alertRuleRepository.findApplicableRules(eq(1L), any())).thenReturn(List.of(rule));
        when(alertsRepository.findByEndpointIdAndRuleIdAndResolvedFalse(1L, 10L)).thenReturn(Optional.empty());

        alertRuleEngine.evaluateRulesForMetric(endpoint, metric);

        ArgumentCaptor<Alerts> captor = ArgumentCaptor.forClass(Alerts.class);
        verify(alertsRepository, times(1)).save(captor.capture());

        Alerts savedAlert = captor.getValue();
        assertEquals(Alerts.Severity.CRITICAL, savedAlert.getSeverity());
        assertEquals(Alerts.AlertType.STATUS_CODE_ERROR, savedAlert.getType());
        assertFalse(savedAlert.isResolved());
        assertTrue(savedAlert.getMessage().contains("Server 5xx Errors"));
    }

    @Test
    void testDuplicateAlertSuppressionForOngoingIncident() {
        Endpoint endpoint = new Endpoint();
        endpoint.setId(1L);

        Metrics metric = new Metrics();
        metric.setStatusCode(500);
        metric.setSuccess(false);

        AlertRule rule = new AlertRule();
        rule.setId(10L);
        rule.setName("500 Internal Error Rule");
        rule.setConditionType(AlertRule.ConditionType.STATUS_CODE);
        rule.setSeverity(Alerts.Severity.CRITICAL);
        rule.setEnabled(true);

        Alerts existingActiveAlert = new Alerts();
        existingActiveAlert.setId(50L);
        existingActiveAlert.setResolved(false);

        when(alertRuleRepository.findApplicableRules(eq(1L), any())).thenReturn(List.of(rule));
        // An active alert already exists!
        when(alertsRepository.findByEndpointIdAndRuleIdAndResolvedFalse(1L, 10L)).thenReturn(Optional.of(existingActiveAlert));

        alertRuleEngine.evaluateRulesForMetric(endpoint, metric);

        // Verify save was NEVER called for a new alert (duplicate prevented)
        verify(alertsRepository, never()).save(any(Alerts.class));
    }

    @Test
    void testAutoResolveWhenMetricRecovers() {
        Endpoint endpoint = new Endpoint();
        endpoint.setId(1L);

        Metrics healthyMetric = new Metrics();
        healthyMetric.setStatusCode(200);
        healthyMetric.setSuccess(true);
        healthyMetric.setResponseTime(150.0);

        AlertRule rule = new AlertRule();
        rule.setId(10L);
        rule.setName("Latency Rule");
        rule.setConditionType(AlertRule.ConditionType.RESPONSE_TIME_THRESHOLD);
        rule.setConditionParameters("500");
        rule.setSeverity(Alerts.Severity.WARNING);
        rule.setEnabled(true);

        Alerts existingActiveAlert = new Alerts();
        existingActiveAlert.setId(50L);
        existingActiveAlert.setResolved(false);

        when(alertRuleRepository.findApplicableRules(eq(1L), any())).thenReturn(List.of(rule));
        when(alertsRepository.findByEndpointIdAndRuleIdAndResolvedFalse(1L, 10L)).thenReturn(Optional.of(existingActiveAlert));

        alertRuleEngine.evaluateRulesForMetric(endpoint, healthyMetric);

        // Verify that existing alert was marked resolved and saved
        assertTrue(existingActiveAlert.isResolved());
        assertNotNull(existingActiveAlert.getResolvedAt());
        verify(alertsRepository, times(1)).save(existingActiveAlert);
    }

    @Test
    void testResponseTimeThresholdTrigger() {
        Endpoint endpoint = new Endpoint();
        endpoint.setId(2L);

        Metrics metric = new Metrics();
        metric.setResponseTime(1800.0);
        metric.setSuccess(true);

        AlertRule rule = new AlertRule();
        rule.setId(25L);
        rule.setName("High Latency Rule");
        rule.setConditionType(AlertRule.ConditionType.RESPONSE_TIME_THRESHOLD);
        rule.setConditionParameters("{\"thresholdMillis\": 1200}");
        rule.setSeverity(Alerts.Severity.WARNING);
        rule.setEnabled(true);

        when(alertRuleRepository.findApplicableRules(eq(2L), any())).thenReturn(List.of(rule));
        when(alertsRepository.findByEndpointIdAndRuleIdAndResolvedFalse(2L, 25L)).thenReturn(Optional.empty());

        alertRuleEngine.evaluateRulesForMetric(endpoint, metric);

        ArgumentCaptor<Alerts> captor = ArgumentCaptor.forClass(Alerts.class);
        verify(alertsRepository, times(1)).save(captor.capture());

        Alerts savedAlert = captor.getValue();
        assertEquals(Alerts.Severity.WARNING, savedAlert.getSeverity());
        assertEquals(Alerts.AlertType.HIGH_LATENCY, savedAlert.getType());
        assertTrue(savedAlert.getMessage().contains("1800 ms exceeded threshold of 1200 ms"));
    }
}
