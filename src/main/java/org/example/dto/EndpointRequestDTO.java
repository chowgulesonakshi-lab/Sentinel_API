package org.example.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.entity.Endpoint;

@Data
public class EndpointRequestDTO {
    @NotNull(message = "HTTP method is required")
    private Endpoint.HttpMethod method;

    @NotBlank(message = "Url cannot be empty")
    private String url;

    @NotNull(message = "Timeout is required")
    @Min(value = 100, message = "Timeout must be at least 100ms")
    private Integer timeoutMillis;

    private boolean enabled;

    @Min(value = 5, message = "Monitoring interval must be at least 5 seconds")
    private Integer intervalSeconds;

    private String expectedStatusCodes;

    @Min(value = 50, message = "Slow response threshold must be at least 50ms")
    private Integer slowResponseThresholdMillis;

    @Min(value = 1, message = "Consecutive failures threshold must be at least 1")
    private Integer consecutiveFailuresThreshold;

    private boolean anomalyDetectionEnabled = true;

    @Min(value = 0, message = "Anomaly sensitivity must not be negative")
    private Double anomalySensitivityMultiplier;

    @Min(value = 1, message = "Anomaly observation window must be at least 1")
    private Integer anomalyObservationWindow;

    @Min(value = 3, message = "Minimum anomaly observations must be at least 3")
    private Integer anomalyMinimumObservations;

    private String headers;

    private String requestBody;

    @NotNull(message = "API Service ID is required")
    private Long apiServiceId;
}
