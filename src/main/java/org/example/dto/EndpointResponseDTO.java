package org.example.dto;

import lombok.Data;
import org.example.entity.Endpoint;

@Data
public class EndpointResponseDTO {
    private Long id;
    private Endpoint.HttpMethod method;
    private String url;
    private Integer timeoutMillis;
    private boolean enabled;
    private Integer intervalSeconds;
    private String expectedStatusCodes;
    private Integer slowResponseThresholdMillis;
    private Integer consecutiveFailuresThreshold;
    private Integer consecutiveFailuresCount;
    private boolean anomalyDetectionEnabled;
    private Double anomalySensitivityMultiplier;
    private Integer anomalyObservationWindow;
    private Integer anomalyMinimumObservations;
    private String headers;
    private String requestBody;
    private java.time.LocalDateTime lastMonitoredAt;

    private Long apiServiceId;
    private String apiServiceName;

    private Long projectId;
    private String projectName;
}
