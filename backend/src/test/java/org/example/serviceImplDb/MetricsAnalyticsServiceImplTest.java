package org.example.serviceImplDb;

import org.example.entity.Metrics;
import org.example.repository.MetricRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetricsAnalyticsServiceImplTest {
    @Mock
    private MetricRepository metricRepository;

    private MetricsAnalyticsServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MetricsAnalyticsServiceImpl(metricRepository);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("sonakshi@example.com", "token", List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void calculatesPercentilesThroughputAvailabilityAndHistoricalComparison() {
        LocalDateTime from = LocalDateTime.of(2026, 11, 17, 12, 0);
        LocalDateTime to = from.plusMinutes(1);
        List<Metrics> current = List.of(metric(100, true), metric(200, true),
                metric(300, true), metric(400, true), metric(500, false));
        List<Metrics> previous = List.of(metric(100, true));

        when(metricRepository
                .findByEndpoint_ApiService_Project_Owner_EmailIgnoreCaseAndTimeStampBetweenOrderByTimeStampDesc(
                        eq("sonakshi@example.com"), any(), any()))
                .thenReturn(current, previous);

        var result = service.getAnalytics(null, from, to);

        assertEquals(5, result.getTotalChecks());
        assertEquals(4, result.getSuccessfulChecks());
        assertEquals(1, result.getFailedChecks());
        assertEquals(250.0, result.getP50LatencyMs());
        assertEquals(370.0, result.getP90LatencyMs());
        assertEquals(385.0, result.getP95LatencyMs());
        assertEquals(397.0, result.getP99LatencyMs());
        assertEquals(20.0, result.getErrorRatePercent());
        assertEquals(80.0, result.getAvailabilityPercent());
        assertEquals(80.0, result.getUptimePercent());
        assertEquals(5.0, result.getThroughputPerMinute());
        assertEquals(400.0, result.getThroughputChangePercent());
        assertEquals(-20.0, result.getAvailabilityChangePercentagePoints());
    }

    @Test
    void rejectsInvalidDateRange() {
        LocalDateTime from = LocalDateTime.of(2026, 11, 17, 12, 0);
        assertThrows(IllegalArgumentException.class,
                () -> service.getAnalytics(null, from, from));
    }

    private Metrics metric(double responseTime, boolean success) {
        Metrics metric = new Metrics();
        metric.setResponseTime(responseTime);
        metric.setSuccess(success);
        return metric;
    }
}
