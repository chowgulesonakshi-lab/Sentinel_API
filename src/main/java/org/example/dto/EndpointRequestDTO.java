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
    @Min(value = 100)
    private Integer timeoutMillis;

    private boolean enabled;

    @NotNull(message = "API Service ID is required")
    private Long apiServiceId;
}
