package org.example.serviceImplDb;

import lombok.RequiredArgsConstructor;
import org.example.dto.MetricsAnalyticsDTO;
import org.example.dto.MetricsResponseDTO;
import org.example.entity.Metrics;
import org.example.repository.MetricRepository;
import org.example.security.ResourceOwnership;
import org.example.service.MetricsAnalyticsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MetricsAnalyticsServiceImpl implements MetricsAnalyticsService {
    private final MetricRepository metricRepository;

    @Override
    public MetricsAnalyticsDTO getAnalytics(Long endpointId, LocalDateTime from, LocalDateTime to) {
        validateRange(from, to);
        LocalDateTime comparisonFrom = from.minus(Duration.between(from, to));
        List<Metrics> current = findOwnedMetrics(endpointId, from, to);
        List<Metrics> previous = findOwnedMetrics(endpointId, comparisonFrom, from);

        MetricsAnalyticsDTO result = summarize(current, from, to, endpointId);
        MetricsAnalyticsDTO baseline = summarize(previous, comparisonFrom, from, endpointId);
        result.setComparisonFrom(comparisonFrom);
        result.setComparisonTo(from);
        result.setPreviousThroughputPerMinute(baseline.getThroughputPerMinute());
        result.setThroughputChangePercent(percentChange(result.getThroughputPerMinute(), baseline.getThroughputPerMinute()));
        result.setPreviousAverageLatencyMs(baseline.getAverageLatencyMs());
        result.setAverageLatencyChangePercent(percentChange(result.getAverageLatencyMs(), baseline.getAverageLatencyMs()));
        result.setPreviousAvailabilityPercent(baseline.getAvailabilityPercent());
        result.setAvailabilityChangePercentagePoints(result.getAvailabilityPercent() - baseline.getAvailabilityPercent());
        return result;
    }

    @Override
    public Page<MetricsResponseDTO> getEndpointHistory(Long endpointId, LocalDateTime from,
                                                       LocalDateTime to, Pageable pageable) {
        validateRange(from, to);
        String email = currentUserEmail();
        return metricRepository
                .findByEndpoint_ApiService_Project_Owner_EmailIgnoreCaseAndEndpoint_IdAndTimeStampBetweenOrderByTimeStampDesc(
                        email, endpointId, from, to, pageable)
                .map(this::toDto);
    }

    private List<Metrics> findOwnedMetrics(Long endpointId, LocalDateTime from, LocalDateTime to) {
        String email = currentUserEmail();
        List<Metrics> metrics = endpointId == null
                ? metricRepository.findByEndpoint_ApiService_Project_Owner_EmailIgnoreCaseAndTimeStampBetweenOrderByTimeStampDesc(
                        email, from, to)
                : metricRepository.findByEndpoint_ApiService_Project_Owner_EmailIgnoreCaseAndEndpoint_IdAndTimeStampBetweenOrderByTimeStampDesc(
                        email, endpointId, from, to);
        return metrics.stream().filter(ResourceOwnership::isCurrentUser).toList();
    }

    private MetricsAnalyticsDTO summarize(List<Metrics> metrics, LocalDateTime from,
                                          LocalDateTime to, Long endpointId) {
        MetricsAnalyticsDTO dto = new MetricsAnalyticsDTO();
        dto.setFrom(from);
        dto.setTo(to);
        dto.setEndpointId(endpointId);
        long total = metrics.size();
        long successful = metrics.stream().filter(Metrics::isSuccess).count();
        long failed = total - successful;
        List<Double> latencies = metrics.stream().filter(Metrics::isSuccess)
                .map(Metrics::getResponseTime).filter(value -> value != null && value >= 0)
                .sorted().toList();
        double average = latencies.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double availability = total == 0 ? 100.0 : successful * 100.0 / total;
        double minutes = Math.max(Duration.between(from, to).toMillis() / 60000.0, 1.0 / 60.0);

        dto.setTotalChecks(total);
        dto.setSuccessfulChecks(successful);
        dto.setFailedChecks(failed);
        dto.setP50LatencyMs(percentile(latencies, 0.50));
        dto.setP90LatencyMs(percentile(latencies, 0.90));
        dto.setP95LatencyMs(percentile(latencies, 0.95));
        dto.setP99LatencyMs(percentile(latencies, 0.99));
        dto.setAverageLatencyMs(average);
        dto.setThroughputPerMinute(total / minutes);
        dto.setErrorRatePercent(total == 0 ? 0.0 : failed * 100.0 / total);
        dto.setUptimePercent(availability);
        dto.setAvailabilityPercent(availability);
        return dto;
    }

    private double percentile(List<Double> sorted, double percentile) {
        if (sorted.isEmpty()) return 0.0;
        if (sorted.size() == 1) return sorted.get(0);
        double index = percentile * (sorted.size() - 1);
        int lower = (int) Math.floor(index);
        int upper = (int) Math.ceil(index);
        if (lower == upper) return sorted.get(lower);
        double fraction = index - lower;
        return sorted.get(lower) + (sorted.get(upper) - sorted.get(lower)) * fraction;
    }

    private double percentChange(double current, double previous) {
        if (previous == 0.0) return current == 0.0 ? 0.0 : 100.0;
        return ((current - previous) / Math.abs(previous)) * 100.0;
    }

    private MetricsResponseDTO toDto(Metrics metric) {
        MetricsResponseDTO dto = new MetricsResponseDTO();
        dto.setId(metric.getId());
        dto.setEndpointId(metric.getEndpoint().getId());
        dto.setEndpointUrl(metric.getEndpoint().getUrl());
        dto.setResponseTime(metric.getResponseTime());
        dto.setSuccess(metric.isSuccess());
        dto.setStatus(metric.getStatus());
        dto.setStatusCode(metric.getStatusCode());
        dto.setTimeStamp(metric.getTimeStamp());
        return dto;
    }

    private void validateRange(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null || !from.isBefore(to)) {
            throw new IllegalArgumentException("'from' must be earlier than 'to'");
        }
    }

    private String currentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
            throw new AccessDeniedException("Authentication is required");
        }
        return authentication.getName();
    }
}
