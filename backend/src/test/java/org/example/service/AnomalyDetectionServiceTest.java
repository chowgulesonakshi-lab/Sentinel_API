package org.example.service;

import org.example.entity.Endpoint;
import org.example.entity.Metrics;
import org.example.repository.MetricRepository;
import org.example.service.anomaly.AnomalyDetectionResult;
import org.example.service.anomaly.AnomalyDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnomalyDetectionServiceTest {
    @Mock
    private MetricRepository metricRepository;

    private AnomalyDetectionService service;

    @BeforeEach
    void setUp() {
        service = new AnomalyDetectionService(metricRepository);
    }

    @Test
    void normalLatencyIsNotAnomaly() {
        Endpoint endpoint = endpoint(5, 3.0);
        Metrics current = metric(100.0, true);
        when(metricRepository.findByEndpointIdOrderByTimeStampDesc(1L)).thenReturn(history(100, 110, 90, 105, 95));

        AnomalyDetectionResult result = service.evaluate(endpoint, current);

        assertTrue(result.isEvaluated());
        assertFalse(result.isAnomalous());
        assertEquals(100.0, result.getHistoricalAverage());
        assertNotNull(result.getExplanation());
    }

    @Test
    void largeEndpointSpecificIncreaseIsAnomaly() {
        Endpoint endpoint = endpoint(5, 2.0);
        Metrics current = metric(450.0, true);
        when(metricRepository.findByEndpointIdOrderByTimeStampDesc(1L)).thenReturn(history(120, 130, 110, 125, 115));

        AnomalyDetectionResult result = service.evaluate(endpoint, current);

        assertTrue(result.isAnomalous());
        assertEquals(120.0, result.getHistoricalAverage());
        assertTrue(result.getObservedResponseTime() > result.getDetectionThreshold());
    }

    @Test
    void insufficientInvalidAndZeroHistoryIsNotEvaluated() {
        Endpoint endpoint = endpoint(5, 3.0);
        Metrics current = metric(450.0, true);
        when(metricRepository.findByEndpointIdOrderByTimeStampDesc(1L)).thenReturn(history(0, -1, null, 120));

        AnomalyDetectionResult result = service.evaluate(endpoint, current);

        assertFalse(result.isEvaluated());
        assertFalse(result.isAnomalous());
        assertEquals(1, result.getObservationCount());
        assertTrue(result.getExplanation().contains("Insufficient"));
    }

    @Test
    void sensitivityChangesDetectionThreshold() {
        Endpoint endpoint = endpoint(5, 1.0);
        Metrics current = metric(140.0, true);
        when(metricRepository.findByEndpointIdOrderByTimeStampDesc(1L)).thenReturn(history(100, 100, 100, 100, 100));

        AnomalyDetectionResult result = service.evaluate(endpoint, current);

        assertTrue(result.isAnomalous());
        assertEquals(100.0, result.getDetectionThreshold());
    }

    @Test
    void failedCurrentCheckIsIgnored() {
        Endpoint endpoint = endpoint(5, 3.0);
        Metrics current = metric(500.0, false);

        AnomalyDetectionResult result = service.evaluate(endpoint, current);

        assertFalse(result.isEvaluated());
        assertFalse(result.isAnomalous());
    }

    private Endpoint endpoint(int minimum, double sensitivity) {
        Endpoint endpoint = new Endpoint();
        endpoint.setId(1L);
        endpoint.setAnomalyDetectionEnabled(true);
        endpoint.setAnomalyMinimumObservations(minimum);
        endpoint.setAnomalyObservationWindow(20);
        endpoint.setAnomalySensitivityMultiplier(sensitivity);
        return endpoint;
    }

    private Metrics metric(Double responseTime, boolean success) {
        Metrics metric = new Metrics();
        metric.setResponseTime(responseTime);
        metric.setSuccess(success);
        metric.setId(999L);
        return metric;
    }

    private List<Metrics> history(Integer... values) {
        List<Metrics> metrics = new ArrayList<>();
        long id = 1;
        for (Integer value : values) {
            Metrics metric = new Metrics();
            metric.setId(id++);
            metric.setSuccess(true);
            metric.setResponseTime(value == null ? null : value.doubleValue());
            metrics.add(metric);
        }
        return metrics;
    }
}
