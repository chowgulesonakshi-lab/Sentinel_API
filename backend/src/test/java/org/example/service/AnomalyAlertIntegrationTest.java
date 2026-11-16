package org.example.service;

import org.example.entity.Alerts;
import org.example.entity.Endpoint;
import org.example.entity.Metrics;
import org.example.repository.EndpointRepository;
import org.example.service.anomaly.AnomalyDetectionResult;
import org.example.service.anomaly.AnomalyDetectionService;
import org.example.serviceImplDb.MonitoringServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnomalyAlertIntegrationTest {
    @Mock private EndpointRepository endpointRepository;
    @Mock private MetricsService metricsService;
    @Mock private AlertsService alertsService;
    @Mock private AnomalyDetectionService anomalyDetectionService;

    private MonitoringServiceImpl monitoringService;

    @BeforeEach
    void setUp() {
        monitoringService = new MonitoringServiceImpl(endpointRepository, metricsService, alertsService, null, anomalyDetectionService);
    }

    @Test
    void createsOnlyOneActiveAnomalyAlert() {
        Endpoint endpoint = endpoint();
        Metrics metric = successfulMetric(450.0);
        when(anomalyDetectionService.evaluate(endpoint, metric)).thenReturn(anomalousResult());
        when(alertsService.getActiveAlert(eq(1L), any(Alerts.AlertType.class))).thenReturn(Optional.empty());

        evaluate(endpoint, metric);

        verify(alertsService).saveAlert(argThat(alert -> alert.getType() == Alerts.AlertType.ANOMALY_DETECTED
                && alert.getMessage().contains("exceeds")
                && alert.getAnomalyHistoricalAverage().equals(120.0)));
    }

    @Test
    void resolvesActiveAnomalyWhenConditionEnds() {
        Endpoint endpoint = endpoint();
        Metrics metric = successfulMetric(125.0);
        Alerts active = new Alerts();
        active.setType(Alerts.AlertType.ANOMALY_DETECTED);
        when(anomalyDetectionService.evaluate(endpoint, metric)).thenReturn(normalResult());
        when(alertsService.getActiveAlert(eq(1L), eq(Alerts.AlertType.ANOMALY_DETECTED))).thenReturn(Optional.of(active));
        when(alertsService.getActiveAlert(eq(1L), eq(Alerts.AlertType.ENDPOINT_DOWN))).thenReturn(Optional.empty());
        when(alertsService.getActiveAlert(eq(1L), eq(Alerts.AlertType.SLOW_RESPONSE))).thenReturn(Optional.empty());

        evaluate(endpoint, metric);

        verify(alertsService).saveAlert(active);
        org.junit.jupiter.api.Assertions.assertTrue(active.isResolved());
    }

    private void evaluate(Endpoint endpoint, Metrics metric) {
        ReflectionTestUtils.invokeMethod(monitoringService, "handleAlertsAndThresholds", endpoint, metric);
    }

    private Endpoint endpoint() {
        Endpoint endpoint = new Endpoint();
        endpoint.setId(1L);
        endpoint.setEnabled(true);
        endpoint.setConsecutiveFailuresCount(0);
        endpoint.setConsecutiveFailuresThreshold(1);
        endpoint.setSlowResponseThresholdMillis(1000);
        return endpoint;
    }

    private Metrics successfulMetric(double responseTime) {
        Metrics metric = new Metrics();
        metric.setId(2L);
        metric.setSuccess(true);
        metric.setResponseTime(responseTime);
        return metric;
    }

    private AnomalyDetectionResult anomalousResult() {
        return AnomalyDetectionResult.builder().evaluated(true).anomalous(true).observationCount(5)
                .observedResponseTime(450.0).historicalAverage(120.0).standardDeviation(15.0)
                .detectionThreshold(165.0).deviationFromAverage(330.0).sensitivityMultiplier(3.0)
                .explanation("Response time 450 ms exceeds the historical threshold.").build();
    }

    private AnomalyDetectionResult normalResult() {
        return AnomalyDetectionResult.builder().evaluated(true).anomalous(false).observationCount(5)
                .observedResponseTime(125.0).historicalAverage(120.0).standardDeviation(15.0)
                .detectionThreshold(165.0).deviationFromAverage(5.0).sensitivityMultiplier(3.0)
                .explanation("Response time is within the historical threshold.").build();
    }
}
