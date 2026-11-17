package org.example.serviceImplDb;

import org.example.dto.SloTargetRequestDTO;
import org.example.entity.Endpoint;
import org.example.entity.Metrics;
import org.example.repository.EndpointRepository;
import org.example.repository.MetricRepository;
import org.example.repository.SloConfigurationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SloServiceImplTest {
    @Mock private EndpointRepository endpointRepository;
    @Mock private MetricRepository metricRepository;
    @Mock private SloConfigurationRepository configurationRepository;

    private SloServiceImpl service;
    private Endpoint endpoint;

    @BeforeEach
    void setUp() {
        service = new SloServiceImpl(endpointRepository, metricRepository, configurationRepository);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("sonakshi@example.com", "token", List.of()));
        endpoint = new Endpoint();
        endpoint.setId(42L);
        endpoint.setUrl("https://api.example.com/health");
        lenient().when(endpointRepository.findByIdAndApiService_Project_Owner_EmailIgnoreCase(
                42L, "sonakshi@example.com")).thenReturn(Optional.of(endpoint));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void reportsAllWindowsAndCalculatesBudgetConsumptionAndBurnRate() {
        List<Metrics> checks = java.util.stream.IntStream.range(0, 1000)
                .mapToObj(i -> metric(i >= 2)).toList();
        when(configurationRepository
                .findByEndpoint_IdAndEndpoint_ApiService_Project_Owner_EmailIgnoreCase(
                        42L, "sonakshi@example.com")).thenReturn(Optional.empty());
        when(metricRepository
                .findByEndpoint_ApiService_Project_Owner_EmailIgnoreCaseAndEndpoint_IdAndTimeStampBetweenOrderByTimeStampDesc(
                        eq("sonakshi@example.com"), eq(42L), any(), any())).thenReturn(checks);

        var report = service.getReport(42L);

        assertEquals(99.9, report.getTargetPercentage());
        assertEquals(3, report.getPeriods().size());
        assertEquals(List.of(7, 30, 90),
                report.getPeriods().stream().map(p -> p.getWindowDays()).toList());
        var week = report.getPeriods().get(0);
        assertEquals(99.8, week.getAvailabilityPercentage(), 0.0001);
        assertEquals(1.0, week.getAllowedFailedChecks(), 0.0001);
        assertEquals(200.0, week.getErrorBudgetConsumedPercentage(), 0.0001);
        assertEquals(0.0, week.getErrorBudgetRemainingPercentage(), 0.0001);
        assertEquals(2.0, week.getBurnRate(), 0.0001);
        assertEquals("BREACHED", week.getStatus());
        assertFalse(week.getCompliant());
    }

    @Test
    void acceptsOnlySupportedSloTargets() {
        SloTargetRequestDTO request = new SloTargetRequestDTO();
        request.setTargetPercentage(99.5);
        assertThrows(IllegalArgumentException.class, () -> service.updateTarget(42L, request));
        verifyNoInteractions(configurationRepository);
    }

    @Test
    void reportsNoDataWithoutClaimingSloCompliance() {
        when(configurationRepository
                .findByEndpoint_IdAndEndpoint_ApiService_Project_Owner_EmailIgnoreCase(
                        42L, "sonakshi@example.com")).thenReturn(Optional.empty());
        when(metricRepository
                .findByEndpoint_ApiService_Project_Owner_EmailIgnoreCaseAndEndpoint_IdAndTimeStampBetweenOrderByTimeStampDesc(
                        eq("sonakshi@example.com"), eq(42L), any(), any())).thenReturn(List.of());

        var report = service.getReport(42L);

        assertEquals("NO_DATA", report.getPeriods().get(0).getStatus());
        assertNull(report.getPeriods().get(0).getCompliant());
    }

    private Metrics metric(boolean success) {
        Metrics metric = new Metrics();
        metric.setSuccess(success);
        return metric;
    }
}
