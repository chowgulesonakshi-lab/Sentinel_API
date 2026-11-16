package org.example.service.ai;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class IncidentContext {
    private Long alertId;
    private String alertType;
    private String severity;
    private String alertMessage;
    private LocalDateTime alertCreatedAt;
    private String ruleName;

    private Long endpointId;
    private String endpointMethod;
    private String endpointUrl;
    private Integer timeoutMillis;
    private Integer intervalSeconds;
    private String expectedStatusCodes;
    private Integer slowResponseThresholdMillis;
    private Integer consecutiveFailuresThreshold;
    private Integer consecutiveFailuresCount;

    private String serviceName;
    private String projectName;

    private Double currentResponseTime;
    private Integer currentStatusCode;
    private Double historicalBaselineAvgResponseTime;
    private Double latencyDeviationPercent;

    private List<MetricSummary> recentMetrics;
    private List<SiblingEndpointSummary> siblingEndpoints;
    private Map<Integer, Long> statusCodeDistribution;
    private List<String> directDependencies;
    private List<String> indirectDependencies;
    private List<String> potentiallyAffectedServices;

    @Data
    @Builder
    public static class MetricSummary {
        private String timestamp;
        private Double responseTime;
        private Integer statusCode;
        private boolean success;
        private String healthStatus;
    }

    @Data
    @Builder
    public static class SiblingEndpointSummary {
        private String method;
        private String url;
        private boolean enabled;
        private Integer lastStatusCode;
        private boolean hasActiveAlert;
    }
}
