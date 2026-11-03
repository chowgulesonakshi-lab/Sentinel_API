package org.example.serviceImplDb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.AIAnalysisResponse;
import org.example.entity.Alerts;
import org.example.repository.AlertsRepository;
import org.example.repository.EndpointRepository;
import org.example.repository.MetricRepository;
import org.example.service.AIService;
import org.example.service.ai.IncidentContext;
import org.example.service.ai.IncidentContextBuilder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

@Service
@Slf4j
public class AIServiceImpl implements AIService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final AlertsRepository alertsRepository;
    private final IncidentContextBuilder incidentContextBuilder;

    public AIServiceImpl(ObjectMapper objectMapper,
                         AlertsRepository alertsRepository,
                         MetricRepository metricRepository,
                         EndpointRepository endpointRepository,
                         IncidentContextBuilder incidentContextBuilder) {
        this.objectMapper = objectMapper;
        this.alertsRepository = alertsRepository;
        this.incidentContextBuilder = incidentContextBuilder;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000); // 3 seconds connect
        requestFactory.setReadTimeout(12000);   // 12 seconds read timeout

        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:11434")
                .requestFactory(requestFactory)
                .build();
    }

    public AIServiceImpl(ObjectMapper objectMapper,
                         AlertsRepository alertsRepository,
                         MetricRepository metricRepository) {
        this(objectMapper, alertsRepository, metricRepository, null,
                new IncidentContextBuilder(metricRepository, null, alertsRepository));
    }

    @Override
    public AIAnalysisResponse analyzeIncident(Long alertId) {
        Alerts alert = alertsRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));

        IncidentContext context = incidentContextBuilder.buildContext(alert);
        String prompt = buildPrompt(context);

        Map<String, Object> request = Map.of(
                "model", "qwen2.5:7b",
                "prompt", prompt,
                "stream", false
        );

        try {
            log.info("Sending incident #{} telemetry context to Ollama (qwen2.5:7b)...", alertId);
            String response = restClient.post()
                    .uri("/api/generate")
                    .body(request)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            JsonNode aiResponseNode = root.get("response");
            if (aiResponseNode == null) {
                throw new RuntimeException("Ollama response does not contain 'response' property");
            }

            String aiResponse = aiResponseNode.asText().trim();

            // Strip Markdown formatting around JSON if present
            if (aiResponse.startsWith("```json")) {
                aiResponse = aiResponse.substring(7);
            } else if (aiResponse.startsWith("```")) {
                aiResponse = aiResponse.substring(3);
            }
            if (aiResponse.endsWith("```")) {
                aiResponse = aiResponse.substring(0, aiResponse.length() - 3);
            }
            aiResponse = aiResponse.trim();

            AIAnalysisResponse result = objectMapper.readValue(aiResponse, AIAnalysisResponse.class);
            result.setAiModel("Qwen 2.5 7B (via Ollama)");

            // Ensure structured fields are not null
            if (result.getSupportingEvidence() == null || result.getSupportingEvidence().isEmpty()) {
                result.setSupportingEvidence(buildEmpiricalEvidenceList(context));
            }
            if (result.getAffectedServices() == null || result.getAffectedServices().isEmpty()) {
                result.setAffectedServices(List.of(context.getServiceName()));
            }
            if (result.getRelatedEndpoints() == null || result.getRelatedEndpoints().isEmpty()) {
                result.setRelatedEndpoints(List.of(context.getEndpointMethod() + " " + context.getEndpointUrl()));
            }

            return result;

        } catch (Exception e) {
            log.warn("Ollama LLM call failed or timed out ({}). Generating data-driven heuristic diagnosis.", e.getMessage());
            return generateDataDrivenFallback(context, alert);
        }
    }

    private String buildPrompt(IncidentContext context) {
        StringBuilder timeline = new StringBuilder();
        if (context.getRecentMetrics() != null) {
            for (IncidentContext.MetricSummary m : context.getRecentMetrics()) {
                timeline.append(String.format("  [%s] Status: %s | Time: %.0f ms | Success: %s | Health: %s\n",
                        m.getTimestamp(), m.getStatusCode(), m.getResponseTime() != null ? m.getResponseTime() : 0.0,
                        m.isSuccess(), m.getHealthStatus()));
            }
        }

        StringBuilder siblings = new StringBuilder();
        if (context.getSiblingEndpoints() != null && !context.getSiblingEndpoints().isEmpty()) {
            for (IncidentContext.SiblingEndpointSummary s : context.getSiblingEndpoints()) {
                siblings.append(String.format("  [%s %s] Status: %s | Alert Active: %s\n",
                        s.getMethod(), s.getUrl(), s.getLastStatusCode() != null ? s.getLastStatusCode() : "None",
                        s.isHasActiveAlert()));
            }
        } else {
            siblings.append("  No sibling endpoints in this service.\n");
        }

        return String.format("""
                You are Sentinel AI, an expert Site Reliability Engineer (SRE) and Incident Intelligence Assistant.
                Analyze the following API monitoring incident using actual empirical telemetry:

                === INCIDENT TELEMETRY ===
                Incident ID: #%d
                Incident Type: %s
                Severity: %s
                Alert Message: %s
                Triggered Rule: %s
                Project: %s | Service: %s
                Target Endpoint: [%s] %s

                === ENDPOINT CONFIGURATION ===
                Configured Request Timeout: %d ms
                Configured Polling Interval: %d seconds
                Expected HTTP Status Codes: %s
                Slow Response Warning Threshold: %d ms
                Consecutive Failures Threshold to Alert: %d
                Current Consecutive Failures Streak: %d

                === EMPIRICAL PERFORMANCE METRICS ===
                Current Latency: %s ms
                Current HTTP Status: %s
                Historical Baseline Average Latency: %s ms
                Latency Spike Deviation: %s%%
                Status Code Distribution in Window: %s

                === RECENT TELEMETRY TIMELINE (Newest to Oldest) ===
                %s
                === SIBLING ENDPOINTS IN SAME SERVICE ===
                %s
                === SERVICE DEPENDENCY EVIDENCE ===
                Direct dependencies: %s
                Indirect dependencies: %s
                Potentially affected downstream services: %s
                === ANALYSIS INSTRUCTIONS ===
                1. EVIDENTIARY RIGOR: Explicitly distinguish EMPIRICAL EVIDENCE (exact numbers, timestamps, status codes) from HYPOTHESES (inferred root causes).
                2. Do NOT declare a root cause confirmed unless explicit status codes or error telemetry prove it.
                3. Provide concrete investigation steps tailored to the error code and latency patterns.
                4. Provide immediate remediation and long-term preventive actions.
                5. Provide a realistic confidence score (0-100) and explain what remains uncertain.

                Return ONLY a valid JSON object matching this schema:
                {
                  "severity": "CRITICAL" | "HIGH" | "MEDIUM" | "LOW",
                  "summary": "Short empirical summary of the incident",
                  "likelyCause": "Most probable hypothesis",
                  "possibleCauses": ["Cause hypothesis 1", "Cause hypothesis 2"],
                  "supportingEvidence": [
                    "Empirical evidence 1 with exact numbers",
                    "Empirical evidence 2 with exact timeline"
                  ],
                  "investigationSteps": [
                    "1. Step one ...",
                    "2. Step two ..."
                  ],
                  "remediationActions": [
                    "1. Action one ...",
                    "2. Action two ..."
                  ],
                  "confidence": 85,
                  "confidenceExplanation": "Explanation of confidence score and remaining uncertainties",
                  "relatedEndpoints": ["%s %s"],
                  "affectedServices": ["%s"]
                }
                Do not include markdown codeblocks or text outside the JSON.
                """,
                context.getAlertId(),
                context.getAlertType(),
                context.getSeverity(),
                context.getAlertMessage(),
                context.getRuleName() != null ? context.getRuleName() : "System Threshold",
                context.getProjectName(),
                context.getServiceName(),
                context.getEndpointMethod(),
                context.getEndpointUrl(),
                context.getTimeoutMillis(),
                context.getIntervalSeconds(),
                context.getExpectedStatusCodes(),
                context.getSlowResponseThresholdMillis(),
                context.getConsecutiveFailuresThreshold(),
                context.getConsecutiveFailuresCount(),
                context.getCurrentResponseTime() != null ? String.format("%.0f", context.getCurrentResponseTime()) : "N/A",
                context.getCurrentStatusCode() != null ? String.valueOf(context.getCurrentStatusCode()) : "N/A",
                context.getHistoricalBaselineAvgResponseTime() != null ? String.format("%.0f", context.getHistoricalBaselineAvgResponseTime()) : "N/A",
                context.getLatencyDeviationPercent() != null ? String.format("%.1f", context.getLatencyDeviationPercent()) : "0",
                context.getStatusCodeDistribution(),
                timeline,
                siblings,
                context.getDirectDependencies(),
                context.getIndirectDependencies(),
                context.getPotentiallyAffectedServices(),
                context.getEndpointMethod(), context.getEndpointUrl(),
                context.getServiceName()
        );
    }

    private List<String> buildEmpiricalEvidenceList(IncidentContext context) {
        List<String> evidence = new ArrayList<>();
        if (context.getCurrentStatusCode() != null) {
            evidence.add(String.format("Latest probe returned HTTP status %d (Expected: %s)",
                    context.getCurrentStatusCode(), context.getExpectedStatusCodes()));
        }
        if (context.getCurrentResponseTime() != null) {
            String latencyMsg = String.format("Current response time is %.0f ms (Configured timeout: %d ms, Slow threshold: %d ms)",
                    context.getCurrentResponseTime(), context.getTimeoutMillis(), context.getSlowResponseThresholdMillis());
            if (context.getHistoricalBaselineAvgResponseTime() != null && context.getLatencyDeviationPercent() != null) {
                latencyMsg += String.format(" — latency is %.1f%% higher than historical baseline of %.0f ms",
                        context.getLatencyDeviationPercent(), context.getHistoricalBaselineAvgResponseTime());
            }
            evidence.add(latencyMsg);
        }
        if (context.getConsecutiveFailuresCount() > 0) {
            evidence.add(String.format("Endpoint failed %d consecutive check(s) (Alert threshold: %d)",
                    context.getConsecutiveFailuresCount(), context.getConsecutiveFailuresThreshold()));
        }

        // Check sibling endpoints
        if (context.getSiblingEndpoints() != null && !context.getSiblingEndpoints().isEmpty()) {
            long failingSiblings = context.getSiblingEndpoints().stream().filter(s -> s.isHasActiveAlert() || (s.getLastStatusCode() != null && s.getLastStatusCode() >= 500)).count();
            if (failingSiblings > 0) {
                evidence.add(String.format("Blast radius: %d other endpoint(s) in service '%s' are also experiencing active incidents",
                        failingSiblings, context.getServiceName()));
            } else {
                evidence.add(String.format("Isolation check: Other endpoints in service '%s' are responding normally (indicates endpoint-specific failure, not full service outage)",
                        context.getServiceName()));
            }
        }

        return evidence;
    }

    private AIAnalysisResponse generateDataDrivenFallback(IncidentContext context, Alerts alert) {
        AIAnalysisResponse fallback = new AIAnalysisResponse();
        fallback.setSeverity(alert.getSeverity() != null ? alert.getSeverity().name() : "CRITICAL");
        fallback.setAiModel("Sentinel Empirical Telemetry Engine (LLM Offline Fallback)");

        List<String> evidence = buildEmpiricalEvidenceList(context);
        fallback.setSupportingEvidence(evidence);

        Integer status = context.getCurrentStatusCode();
        Double latency = context.getCurrentResponseTime();

        if (status != null && status == 504) {
            fallback.setSummary(String.format("Gateway Timeout (HTTP 504) on %s %s: Request exceeded %d ms limit.",
                    context.getEndpointMethod(), context.getEndpointUrl(), context.getTimeoutMillis()));
            fallback.setLikelyCause("Upstream server or database query stalled, causing gateway reverse proxy timeout.");
            fallback.setPossibleCauses(List.of(
                    "Database connection pool exhaustion or unindexed query lock",
                    "Downstream microservice dependency failure or network timeout",
                    "Thread pool saturation on application server"
            ));
            fallback.setRecommendation("Check backend server thread pools, active DB lock contention, and reverse-proxy timeout thresholds.");
            fallback.setInvestigationSteps(List.of(
                    "1. Inspect application thread dump for BLOCKED or WAITING database connections.",
                    "2. Check reverse proxy (Nginx/Gateway) timeout settings against backend response times.",
                    "3. Review database slow query logs for unindexed long-running queries around " + context.getAlertCreatedAt()
            ));
            fallback.setRemediationActions(List.of(
                    "Temporarily increase timeout limit or scale backend service replicas.",
                    "Implement circuit breaker (Resilience4j) to fail fast on unresponsive dependencies.",
                    "Restart saturated application containers if thread deadlocks are identified."
            ));
            fallback.setConfidence(85);
            fallback.setConfidenceExplanation("High confidence based on exact HTTP 504 Gateway Timeout and latency exceeding configured limit.");

        } else if (status != null && status == 503) {
            fallback.setSummary(String.format("Service Unavailable (HTTP 503): %s %s refused connection.",
                    context.getEndpointMethod(), context.getEndpointUrl()));
            fallback.setLikelyCause("Service process is stopped, restarting, or rejecting connections due to capacity limits.");
            fallback.setPossibleCauses(List.of(
                    "Application process crashed or OOMKilled (Out of Memory)",
                    "Service pod is in CrashLoopBackOff or restart cycle",
                    "Network security group or firewall blocking inbound traffic"
            ));
            fallback.setRecommendation("Verify container/process status and inspect host memory and CPU utilization.");
            fallback.setInvestigationSteps(List.of(
                    "1. Run `docker ps` or `kubectl get pods` to verify container health.",
                    "2. Check system error logs (`journalctl` / `kubectl logs`) for OutOfMemory (OOM) errors.",
                    "3. Verify network ingress routing and load balancer target group health checks."
            ));
            fallback.setRemediationActions(List.of(
                    "Restart the service process or allocate increased memory limits.",
                    "Redeploy previous stable build if recent deployment caused crash on startup."
            ));
            fallback.setConfidence(90);
            fallback.setConfidenceExplanation("High confidence based on connection refusal / 503 status code.");

        } else if (status != null && status >= 500) {
            fallback.setSummary(String.format("Internal Server Error (HTTP %d) on %s %s.",
                    status, context.getEndpointMethod(), context.getEndpointUrl()));
            fallback.setLikelyCause("Unhandled server-side exception or unrecoverable application error.");
            fallback.setPossibleCauses(List.of(
                    "NullPointerException or unhandled runtime exception in request controller",
                    "Database syntax error or schema mismatch following recent migration",
                    "Third-party integration exception or missing environment variable"
            ));
            fallback.setRecommendation("Inspect backend application exception stack traces in logs.");
            fallback.setInvestigationSteps(List.of(
                    "1. Search application logs for 'ERROR' or 'Exception' around " + context.getAlertCreatedAt(),
                    "2. Verify recent configuration changes and environment secret bindings.",
                    "3. Validate request payload against API schema expectations."
            ));
            fallback.setRemediationActions(List.of(
                    "Hotfix unhandled exception handling and return appropriate client status codes.",
                    "Roll back recent migration if database schema mismatch caused internal failure."
            ));
            fallback.setConfidence(80);
            fallback.setConfidenceExplanation("Confidence based on persistent 5xx HTTP response codes recorded in telemetry.");

        } else if (latency != null && latency > context.getSlowResponseThresholdMillis()) {
            fallback.setSummary(String.format("Performance Degradation: Latency (%.0f ms) exceeded threshold (%d ms).",
                    latency, context.getSlowResponseThresholdMillis()));
            fallback.setLikelyCause("Resource contention, garbage collection pauses, or inefficient I/O operations.");
            fallback.setPossibleCauses(List.of(
                    "JVM Garbage Collection (GC) stop-the-world pauses",
                    "CPU throttling on host machine or container",
                    "High database disk I/O latency or missing indexes"
            ));
            fallback.setRecommendation("Profile endpoint execution with APM tooling and inspect host resource metrics.");
            fallback.setInvestigationSteps(List.of(
                    "1. Inspect JVM GC metrics for high pause durations.",
                    "2. Check CPU and memory utilization graphs on the host service.",
                    "3. Profile database query execution plans (`EXPLAIN ANALYZE`)."
            ));
            fallback.setRemediationActions(List.of(
                    "Add database indexing for queried columns.",
                    "Tune JVM heap allocation and garbage collector settings (e.g. G1GC).",
                    "Enable HTTP response caching for static or idempotent query responses."
            ));
            fallback.setConfidence(75);
            fallback.setConfidenceExplanation("Latency metrics confirmed slow execution, but exact subsystem bottleneck requires APM profiling.");

        } else {
            fallback.setSummary(String.format("API Incident Detected: %s", alert.getMessage()));
            fallback.setLikelyCause("Endpoint failure violating configured health thresholds.");
            fallback.setPossibleCauses(List.of("Transient network blip", "Target API downtime", "Invalid request routing"));
            fallback.setRecommendation("Review monitoring logs and verify target endpoint availability.");
            fallback.setInvestigationSteps(List.of("1. Test endpoint connectivity manually.", "2. Review recent metrics history."));
            fallback.setRemediationActions(List.of("Confirm target server status.", "Adjust threshold if endpoint is inherently slow."));
            fallback.setConfidence(65);
            fallback.setConfidenceExplanation("General telemetry-based analysis.");
        }

        fallback.setRelatedEndpoints(List.of(context.getEndpointMethod() + " " + context.getEndpointUrl()));
        fallback.setAffectedServices(List.of(context.getServiceName()));

        return fallback;
    }
}
