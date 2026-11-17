package org.example.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MetricsAnalyticsDTO {
    private LocalDateTime from;
    private LocalDateTime to;
    private LocalDateTime comparisonFrom;
    private LocalDateTime comparisonTo;
    private Long endpointId;
    private long totalChecks;
    private long successfulChecks;
    private long failedChecks;
    private double p50LatencyMs;
    private double p90LatencyMs;
    private double p95LatencyMs;
    private double p99LatencyMs;
    private double averageLatencyMs;
    private double throughputPerMinute;
    private double errorRatePercent;
    private double uptimePercent;
    private double availabilityPercent;
    private double previousThroughputPerMinute;
    private double throughputChangePercent;
    private double previousAverageLatencyMs;
    private double averageLatencyChangePercent;
    private double previousAvailabilityPercent;
    private double availabilityChangePercentagePoints;
}
