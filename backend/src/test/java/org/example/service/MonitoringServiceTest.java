package org.example.service;

import org.example.entity.Alerts;
import org.example.entity.ApiService;
import org.example.entity.Endpoint;
import org.example.entity.Metrics;
import org.example.repository.EndpointRepository;
import org.example.serviceImplDb.MonitoringServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MonitoringServiceTest {

    @Mock
    private EndpointRepository endpointRepository;

    @Mock
    private MetricsService metricsService;

    @Mock
    private AlertsService alertsService;

    private MonitoringServiceImpl monitoringService;

    @BeforeEach
    void setUp() {
        monitoringService = new MonitoringServiceImpl(endpointRepository, metricsService, alertsService);
    }

    @Test
    void testMonitorDueEndpointsFiltersProperly() {
        Endpoint notDue = new Endpoint();
        notDue.setId(1L);
        notDue.setEnabled(true);
        notDue.setUrl("https://httpbin.org/get");
        notDue.setMethod(Endpoint.HttpMethod.GET);
        notDue.setIntervalSeconds(120);
        notDue.setLastMonitoredAt(LocalDateTime.now().minusSeconds(10)); // Monitored 10s ago, interval 120s -> NOT due

        Endpoint due = new Endpoint();
        due.setId(2L);
        due.setEnabled(true);
        due.setUrl("https://httpbin.org/get");
        due.setMethod(Endpoint.HttpMethod.GET);
        due.setIntervalSeconds(30);
        due.setLastMonitoredAt(LocalDateTime.now().minusSeconds(40)); // Monitored 40s ago, interval 30s -> DUE

        when(endpointRepository.findByEnabledTrue()).thenReturn(List.of(notDue, due));

        monitoringService.monitorDueEndpoints();

        // Verify save was called only for the due endpoint
        verify(endpointRepository, times(1)).save(due);
        verify(endpointRepository, never()).save(notDue);
    }

    @Test
    void testConsecutiveFailuresThresholdSuppressesAlertUntilReached() {
        Endpoint endpoint = new Endpoint();
        endpoint.setId(10L);
        endpoint.setUrl("http://127.0.0.1:54321/unreachable"); // will fail connection
        endpoint.setMethod(Endpoint.HttpMethod.GET);
        endpoint.setTimeoutMillis(500);
        endpoint.setEnabled(true);
        endpoint.setConsecutiveFailuresThreshold(3);
        endpoint.setConsecutiveFailuresCount(0);

        when(endpointRepository.save(any(Endpoint.class))).thenReturn(endpoint);

        // First failure -> count becomes 1, no alert
        monitoringService.monitorEndpoint(endpoint);
        assertEquals(1, endpoint.getConsecutiveFailuresCount());
        verify(alertsService, never()).saveAlert(any(Alerts.class));

        // Second failure -> count becomes 2, still no alert
        monitoringService.monitorEndpoint(endpoint);
        assertEquals(2, endpoint.getConsecutiveFailuresCount());
        verify(alertsService, never()).saveAlert(any(Alerts.class));

        // Third failure -> count becomes 3, threshold reached -> alert triggered
        monitoringService.monitorEndpoint(endpoint);
        assertEquals(3, endpoint.getConsecutiveFailuresCount());
        verify(alertsService, times(1)).saveAlert(any(Alerts.class));
    }

    @Test
    void testSuccessResetsConsecutiveFailures() {
        Endpoint endpoint = new Endpoint();
        endpoint.setId(15L);
        endpoint.setUrl("https://jsonplaceholder.typicode.com/todos/1");
        endpoint.setMethod(Endpoint.HttpMethod.GET);
        endpoint.setTimeoutMillis(5000);
        endpoint.setEnabled(true);
        endpoint.setConsecutiveFailuresThreshold(3);
        endpoint.setConsecutiveFailuresCount(2); // Was previously failing
        endpoint.setExpectedStatusCodes("200");

        when(alertsService.getActiveAlert(eq(15L), any(Alerts.AlertType.class))).thenReturn(Optional.empty());
        when(endpointRepository.save(any(Endpoint.class))).thenReturn(endpoint);

        Metrics result = monitoringService.monitorEndpoint(endpoint);

        // Should reset consecutive failure count
        assertEquals(0, endpoint.getConsecutiveFailuresCount());
        assertNotNull(endpoint.getLastMonitoredAt());
    }

    @Test
    void testCustomExpectedStatusCodeAcceptsMatchingCode() {
        // Test that expected status codes like "404" or "400-499" are treated as success if configured
        Endpoint endpoint = new Endpoint();
        endpoint.setId(20L);
        endpoint.setUrl("https://jsonplaceholder.typicode.com/invalid-path-that-returns-404");
        endpoint.setMethod(Endpoint.HttpMethod.GET);
        endpoint.setTimeoutMillis(5000);
        endpoint.setEnabled(true);
        endpoint.setExpectedStatusCodes("404");

        when(alertsService.getActiveAlert(eq(20L), any(Alerts.AlertType.class))).thenReturn(Optional.empty());
        when(endpointRepository.save(any(Endpoint.class))).thenReturn(endpoint);

        Metrics metric = monitoringService.monitorEndpoint(endpoint);

        assertEquals(404, metric.getStatusCode());
        assertTrue(metric.isSuccess(), "Configured expected status 404 should be treated as success");
        assertEquals(Metrics.HealthStatus.UP, metric.getStatus());
    }
}
