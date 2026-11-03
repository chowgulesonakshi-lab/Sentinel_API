package org.example.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ServiceDependencyResponseDTO {
    private Long id;
    private Long projectId;
    private Long dependentServiceId;
    private String dependentServiceName;
    private Long dependencyServiceId;
    private String dependencyServiceName;
    private LocalDateTime createdAt;
}
