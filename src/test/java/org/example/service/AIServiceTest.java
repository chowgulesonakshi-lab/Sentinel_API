package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.AIAnalysisResponse;
import org.example.entity.Alerts;
import org.example.entity.ApiService;
import org.example.entity.Endpoint;
import org.example.entity.Metrics;
import org.example.repository.AlertsRepository;
import org.example.repository.EndpointRepository;
import org.example.repository.MetricRepository;
import org.example.service.ai.IncidentContext;
import org.example.service.ai.IncidentContextBuilder;
import org.example.serviceImplDb.AIServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AIServiceTest {

    @Mock
    private AlertsRepository alertsRepository;

    @Mock
    private MetricRepository metricRepository;

    @Mock
    private EndpointRepository endpointRepository;

    private IncidentContextBuilder incidentContextBuilder;
    private AIServiceImpl aiService;

    @BeforeEach
    void setUp() {
        incidentContextBuilder = new IncidentContextBuilder(metricRepository, endpointRepository, alertsRepository);
        aiService = new AIServiceImpl(new ObjectMapper(), alertsRepository, metricRepository, endpointRepository, incidentContextBuilder);
    }

    @Test
    void testIncidentContextBuilderComputesBaselineAndEvidence() {
        ApiService service = new ApiService();
        service.setId(1L);
        service.setName("Checkout Service");

        Endpoint endpoint = new Endpoint();
        endpoint.setId(10L);
        endpoint.setUrl("https://api.example.com/checkout");
        endpoint.setMethod(Endpoint.HttpMethod.POST);
        endpoint.setTimeoutMillis(5000);
        endpoint.setSlowResponseThresholdMillis(1000);
        endpoint.setExpectedStatusCodes("200,201");
        endpoint.setConsecutiveFailuresThreshold(2);
        endpoint.setConsecutiveFailuresCount(2);
        endpoint.setApiService(service);

        Alerts alert = new Alerts();
        alert.setId(99L);
        alert.setEndpoint(endpoint);
        alert.setSeverity(Alerts.Severity.CRITICAL);
        alert.setType(Alerts.AlertType.ENDPOINT_DOWN);
        alert.setMessage("Endpoint failed 2 consecutive checks with HTTP 504");
        alert.setCreatedAt(LocalDateTime.now());

        // 3 historical metrics: 2 successes (100ms, 200ms -> avg 150ms), 1 latest failure (5000ms, 504)
        Metrics m1 = new Metrics();
        m1.setId(1L);
        m1.setSuccess(false);
        m1.setStatusCode(504);
        m1.setResponseTime(5000.0);
        m1.setTimeStamp(LocalDateTime.now());

        Metrics m2 = new Metrics();
        m2.setId(2L);
        m2.setSuccess(true);
        m2.setStatusCode(200);
        m2.setResponseTime(100.0);
        m2.setTimeStamp(LocalDateTime.now().minusMinutes(1));

        Metrics m3 = new Metrics();
        m3.setId(3L);
        m3.setSuccess(true);
        m3.setStatusCode(200);
        m3.setResponseTime(200.0);
        m3.setTimeStamp(LocalDateTime.now().minusMinutes(2));

        when(metricRepository.findTop10ByEndpointIdOrderByTimeStampDesc(10L)).thenReturn(List.of(m1, m2, m3));
        when(endpointRepository.findByApiServiceId(1L)).thenReturn(List.of(endpoint));

        IncidentContext context = incidentContextBuilder.buildContext(alert);

        assertEquals(99L, context.getAlertId());
        assertEquals("Checkout Service", context.getServiceName());
        assertEquals(504, context.getCurrentStatusCode());
        assertEquals(5000.0, context.getCurrentResponseTime());
        assertEquals(150.0, context.getHistoricalBaselineAvgResponseTime()); // (100 + 200) / 2
        assertNotNull(context.getLatencyDeviationPercent());
        assertTrue(context.getLatencyDeviationPercent() > 1000.0); // 5000 vs 150 is > 3000% higher
    }

    @Test
    void testAIAnalysisFallbackProvidesEmpiricalEvidenceAndSteps() {
        ApiService service = new ApiService();
        service.setId(1L);
        service.setName("Payment Gateway");

        Endpoint endpoint = new Endpoint();
        endpoint.setId(20L);
        endpoint.setUrl("https://api.example.com/pay");
        endpoint.setMethod(Endpoint.HttpMethod.POST);
        endpoint.setTimeoutMillis(5000);
        endpoint.setSlowResponseThresholdMillis(1000);
        endpoint.setExpectedStatusCodes("200");
        endpoint.setConsecutiveFailuresThreshold(3);
        endpoint.setConsecutiveFailuresCount(3);
        endpoint.setApiService(service);

        Alerts alert = new Alerts();
        alert.setId(88L);
        alert.setEndpoint(endpoint);
        alert.setSeverity(Alerts.Severity.CRITICAL);
        alert.setType(Alerts.AlertType.ENDPOINT_DOWN);
        alert.setMessage("Endpoint failed 3 consecutive checks with HTTP 504");
        alert.setCreatedAt(LocalDateTime.now());

        Metrics latest = new Metrics();
        latest.setId(1L);
        latest.setSuccess(false);
        latest.setStatusCode(504);
        latest.setResponseTime(5002.0);
        latest.setTimeStamp(LocalDateTime.now());

        when(alertsRepository.findById(88L)).thenReturn(Optional.of(alert));
        when(metricRepository.findTop10ByEndpointIdOrderByTimeStampDesc(20L)).thenReturn(List.of(latest));
        when(endpointRepository.findByApiServiceId(1L)).thenReturn(List.of(endpoint));

        // When Ollama is offline or uncontactable, fallback analysis is generated
        AIAnalysisResponse response = aiService.analyzeIncident(88L);

        assertNotNull(response);
        assertEquals("CRITICAL", response.getSeverity());
        assertNotNull(response.getSupportingEvidence());
        assertFalse(response.getSupportingEvidence().isEmpty(), "Supporting evidence must not be empty");
        assertTrue(response.getSupportingEvidence().get(0).contains("HTTP status 504"));

        assertNotNull(response.getInvestigationSteps());
        assertFalse(response.getInvestigationSteps().isEmpty());

        assertNotNull(response.getRemediationActions());
        assertFalse(response.getRemediationActions().isEmpty());

        assertNotNull(response.getConfidenceExplanation());
        assertTrue(response.getConfidence() >= 70);
    }
}
