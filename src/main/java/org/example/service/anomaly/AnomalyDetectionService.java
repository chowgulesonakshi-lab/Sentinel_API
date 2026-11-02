package org.example.service.anomaly;

import lombok.RequiredArgsConstructor;
import org.example.entity.Endpoint;
import org.example.entity.Metrics;
import org.example.repository.MetricRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnomalyDetectionService {
    private final MetricRepository metricRepository;

    public AnomalyDetectionResult evaluate(Endpoint endpoint, Metrics currentMetric) {
        double current = currentMetric.getResponseTime() == null ? Double.NaN : currentMetric.getResponseTime();
        double sensitivity = positiveOrDefault(endpoint.getAnomalySensitivityMultiplier(), 3.0);
        int window = positiveOrDefault(endpoint.getAnomalyObservationWindow(), 20);
        int minimum = Math.max(3, positiveOrDefault(endpoint.getAnomalyMinimumObservations(), 5));

        if (Boolean.FALSE.equals(endpoint.getAnomalyDetectionEnabled())) {
            return notEvaluated(current, sensitivity, "Anomaly detection is disabled for this endpoint.");
        }
        if (!Double.isFinite(current) || current <= 0 || !currentMetric.isSuccess()) {
            return notEvaluated(current, sensitivity, "The current response time is missing, non-positive, invalid, or the check failed.");
        }

        List<Double> history = metricRepository.findByEndpointIdOrderByTimeStampDesc(endpoint.getId()).stream()
                .filter(metric -> metric != currentMetric && (currentMetric.getId() == null || !currentMetric.getId().equals(metric.getId())))
                .map(Metrics::getResponseTime)
                .filter(value -> value != null && Double.isFinite(value) && value > 0)
                .limit(window)
                .toList();

        if (history.size() < minimum) {
            return AnomalyDetectionResult.builder()
                    .evaluated(false)
                    .anomalous(false)
                    .observationCount(history.size())
                    .observedResponseTime(current)
                    .sensitivityMultiplier(sensitivity)
                    .explanation(String.format("Insufficient valid history: %d of %d observations required.", history.size(), minimum))
                    .build();
        }

        double average = history.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = history.stream()
                .mapToDouble(value -> Math.pow(value - average, 2))
                .average()
                .orElse(0.0);
        double standardDeviation = Math.sqrt(variance);
        double threshold = average + sensitivity * standardDeviation;
        boolean anomalous = current > threshold && current > average;
        double deviation = current - average;
        String explanation = anomalous
                ? String.format("Response time %.0f ms is %.0f ms above the historical average and exceeds the %.1fσ threshold of %.0f ms.", current, deviation, sensitivity, threshold)
                : String.format("Response time %.0f ms is within the historical threshold of %.0f ms.", current, threshold);

        return AnomalyDetectionResult.builder()
                .evaluated(true)
                .anomalous(anomalous)
                .observationCount(history.size())
                .observedResponseTime(current)
                .historicalAverage(average)
                .standardDeviation(standardDeviation)
                .detectionThreshold(threshold)
                .deviationFromAverage(deviation)
                .sensitivityMultiplier(sensitivity)
                .explanation(explanation)
                .build();
    }

    private AnomalyDetectionResult notEvaluated(double current, double sensitivity, String explanation) {
        return AnomalyDetectionResult.builder()
                .evaluated(false)
                .anomalous(false)
                .observedResponseTime(Double.isFinite(current) ? current : null)
                .sensitivityMultiplier(sensitivity)
                .explanation(explanation)
                .build();
    }

    private int positiveOrDefault(Integer value, int fallback) {
        return value != null && value > 0 ? value : fallback;
    }

    private double positiveOrDefault(Double value, double fallback) {
        return value != null && Double.isFinite(value) && value > 0 ? value : fallback;
    }
}
