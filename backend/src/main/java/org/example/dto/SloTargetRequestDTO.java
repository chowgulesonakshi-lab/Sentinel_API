package org.example.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SloTargetRequestDTO {
    @NotNull(message = "SLO target percentage is required")
    private Double targetPercentage;
}
