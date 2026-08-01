package org.example.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DashboardResponseDTO {
    private long totalProjects;
    private long totalApiServices;
    private long totalEndpoints;

    private long successfulChecks;
    private long failedChecks;
    private double averageResponseTime;

    private LocalDateTime lastHealthCheck;
}