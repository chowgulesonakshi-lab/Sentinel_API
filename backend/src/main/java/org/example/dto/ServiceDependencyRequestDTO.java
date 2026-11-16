package org.example.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ServiceDependencyRequestDTO {
    @NotNull(message = "Dependent service ID is required")
    private Long dependentServiceId;

    @NotNull(message = "Dependency service ID is required")
    private Long dependencyServiceId;
}
