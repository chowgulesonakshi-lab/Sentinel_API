package org.example.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SloPeriodReportDTO {
    private int windowDays;
    private LocalDateTime from;
    private LocalDateTime to;
    private long totalChecks;
    private long successfulChecks;
    private long failedChecks;
    private double targetPercentage;
    private double availabilityPercentage;
    private double allowedFailedChecks;
    private double errorBudgetConsumedPercentage;
    private double errorBudgetRemainingPercentage;
    private double burnRate;
    private Boolean compliant;
    private String status;
}
