package org.example.service.anomaly;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AnomalyDetectionResult {
    boolean evaluated;
    boolean anomalous;
    int observationCount;
    Double observedResponseTime;
    Double historicalAverage;
    Double standardDeviation;
    Double detectionThreshold;
    Double deviationFromAverage;
    Double sensitivityMultiplier;
    String explanation;
}
