package org.example.dto;

import lombok.Data;
import org.example.entity.ApiService;

@Data
public class ApiServiceResponseDTO {
    private Long id;
    private String name;
    private String description;

    private Long projectId;
    private String projectName;

    private ApiService.ServiceStatus status;
}
