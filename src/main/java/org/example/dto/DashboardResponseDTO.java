package org.example.dto;

import lombok.Data;
import org.example.entity.Alerts;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DashboardResponseDTO {

    private long totalProjects;
    private long totalApiServices;
    private long totalEndpoints;

    private long successfulChecks;
    private long failedChecks;

    private double averageResponseTime;

    private LocalDateTime lastHealthCheck;

    private List<Alerts> alerts;

    private long activeAlerts;

}