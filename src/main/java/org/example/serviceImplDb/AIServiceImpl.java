package org.example.serviceImplDb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.AIAnalysisResponse;
import org.example.entity.Alerts;
import org.example.entity.Metrics;
import org.example.repository.AlertsRepository;
import org.example.repository.MetricRepository;
import org.example.service.AIService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.Map;

@Service
public class AIServiceImpl implements AIService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final AlertsRepository alertsRepository;
    private final MetricRepository metricRepository;

    public AIServiceImpl(ObjectMapper objectMapper, AlertsRepository alertsRepository,
            MetricRepository metricRepository) {
        this.alertsRepository = alertsRepository;
        this.metricRepository = metricRepository;
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:11434")
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public AIAnalysisResponse analyzeIncident(Long alertId) {
        Alerts alert = alertsRepository.findById(alertId)
                .orElseThrow(() ->
                        new RuntimeException("Alert not found: " + alertId));
        Long endpointId = alert.getEndpoint().getId();
        var metrics = metricRepository
                .findTop10ByEndpointIdOrderByTimeStampDesc(endpointId);
        StringBuilder metricsData = new StringBuilder();
        for (Metrics metric : metrics) {
            metricsData.append("""
                    Response Time: %s ms
                    Success: %s
                    HTTP Status: %s
                    Health: %s
                    Timestamp: %s
                    """.formatted(
                    metric.getResponseTime(),
                    metric.isSuccess(),
                    metric.getStatusCode(),
                    metric.getStatus(),
                    metric.getTimeStamp()));
        }
        String incidentData = """
                Alert ID: %d
                Alert Type: %s
                Severity: %s
                Message: %s
                Resolved: %s
                Created At: %s

                Recent Metrics:
                %s
                """.formatted(
                alert.getId(),
                alert.getType(),
                alert.getSeverity(),
                alert.getMessage(),
                alert.isResolved(),
                alert.getCreatedAt(),
                metricsData);

        String prompt = """
                You are Sentinel AI, an API monitoring and incident analysis assistant.

                Analyze the following API monitoring incident:

                %s

                Return ONLY valid JSON:
                {
                  "severity": "LOW | MEDIUM | HIGH | CRITICAL",
                  "summary": "short explanation",
                  "likelyCause": "most likely cause",
                  "possibleCauses": [
                    "cause 1",
                    "cause 2"
                  ],
                  "recommendation": "recommended action",
                  "confidence": 0
                }

                Confidence must be an integer between 0 and 100.
                Do not include markdown.
                Do not include explanations outside the JSON.
                """.formatted(incidentData);
        Map<String, Object> request = Map.of(
                "model", "qwen2.5:7b",
                "prompt", prompt,
                "stream", false);
        try {
            String response = restClient.post()
                    .uri("/api/generate")
                    .body(request)
                    .retrieve()
                    .body(String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode aiResponseNode = root.get("response");
            if (aiResponseNode == null) {
                throw new RuntimeException(
                        "Ollama response does not contain 'response'"
                );
            }
            String aiResponse = aiResponseNode.asText().trim();

            // Strip any accidental markdown formatting around JSON
            if (aiResponse.startsWith("```json")) {
                aiResponse = aiResponse.substring(7);
            } else if (aiResponse.startsWith("```")) {
                aiResponse = aiResponse.substring(3);
            }
            if (aiResponse.endsWith("```")) {
                aiResponse = aiResponse.substring(0, aiResponse.length() - 3);
            }
            aiResponse = aiResponse.trim();

            return objectMapper.readValue(
                    aiResponse,
                    AIAnalysisResponse.class);
        } catch (Exception e) {
            AIAnalysisResponse fallback = new AIAnalysisResponse();
            fallback.setSeverity(alert.getSeverity() != null ? alert.getSeverity().name() : "MEDIUM");
            fallback.setSummary("AI Analysis Unavailable: Ollama Service Unreachable");
            fallback.setLikelyCause("Local Ollama model 'qwen2.5:7b' server is not running on http://localhost:11434.");
            fallback.setPossibleCauses(java.util.List.of(
                    "Ollama process is stopped",
                    "Model 'qwen2.5:7b' is not downloaded",
                    "Network connection issue to local LLM port 11434"
            ));
            fallback.setRecommendation("Start Ollama service via 'ollama serve' and ensure model 'qwen2.5:7b' is pulled.");
            fallback.setConfidence(0);
            return fallback;
        }
    }
}