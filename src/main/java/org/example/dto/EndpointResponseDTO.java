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

    private Long apiServiceId;
    private String apiServiceName;

    private Long projectId;
    private String projectName;
}
