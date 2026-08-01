package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.entity.ApiService;

@Data
public class ApiServiceRequestDTO {
    @NotBlank(message = "Name cannot be empty")
    private String name;

    @NotBlank(message = "Description cannot be empty")
    private String description;

    @NotNull(message = "Project Id is required")
    private Long projectId;

    @NotNull(message = "Status is required")
    private ApiService.ServiceStatus status;
}
