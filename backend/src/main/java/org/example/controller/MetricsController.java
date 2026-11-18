package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.MetricsAnalyticsDTO;
import org.example.dto.MetricsResponseDTO;
import org.example.service.MetricsAnalyticsService;
import org.example.service.MetricsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/metrics")
@RequiredArgsConstructor

public class MetricsController {

    private final MetricsService metricsService;
    private final MetricsAnalyticsService metricsAnalyticsService;

    @GetMapping
    public ResponseEntity<List<MetricsResponseDTO>> getAllMetrics() {
        return ResponseEntity.ok(metricsService.getAllMetrics());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MetricsResponseDTO> getMetricById(@PathVariable Long id) {
        return metricsService.getMetricById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/endpoint/{endpointId}")
    public ResponseEntity<List<MetricsResponseDTO>> getMetricsByEndpoint(@PathVariable Long endpointId) {
        return ResponseEntity.ok(metricsService.getMetricsByEndpoint(endpointId));
    }

    @GetMapping("/endpoint/{endpointId}/history")
    public ResponseEntity<Page<MetricsResponseDTO>> getEndpointHistory(
            @PathVariable Long endpointId,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            @PageableDefault(size = 50, sort = "timeStamp", direction = Sort.Direction.DESC) Pageable pageable) {
        LocalDateTime end = to != null ? to : LocalDateTime.now();
        LocalDateTime start = from != null ? from : end.minusDays(1);
        return ResponseEntity.ok(metricsAnalyticsService.getEndpointHistory(endpointId, start, end, pageable));
    }

    @GetMapping("/analytics")
    public ResponseEntity<MetricsAnalyticsDTO> getAnalytics(
            @RequestParam(required = false) Long endpointId,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to) {
        LocalDateTime end = to != null ? to : LocalDateTime.now();
        LocalDateTime start = from != null ? from : end.minusHours(24);
        return ResponseEntity.ok(metricsAnalyticsService.getAnalytics(endpointId, start, end));
    }

    @GetMapping("/slow")
    public ResponseEntity<List<MetricsResponseDTO>> getSlowMetrics(@RequestParam double responseTime) {
        return ResponseEntity.ok(metricsService.getSlowMetrics(responseTime));
    }

    @GetMapping("/status/{statusCode}")
    public ResponseEntity<List<MetricsResponseDTO>> getMetricsByStatusCode(@PathVariable int statusCode) {
        return ResponseEntity.ok(metricsService.getMetricsByStatusCode(statusCode));
    }

    @GetMapping("/latest")
    public ResponseEntity<List<MetricsResponseDTO>> getLatestMetrics() {
        return ResponseEntity.ok(metricsService.getLatestMetrics());
    }

    @GetMapping("/endpoint/{endpointId}/latest")
    public ResponseEntity<MetricsResponseDTO> getLatestMetricForEndpoint(@PathVariable Long endpointId) {
        return metricsService.getLatestMetricForEndpoint(endpointId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/average-response-time")
    public ResponseEntity<Double> getAverageResponseTime() {
        return ResponseEntity.ok(metricsService.getAverageResponseTime());
    }

    @GetMapping("/last-health-check")
    public ResponseEntity<LocalDateTime> getLastHealthCheck() {
        return ResponseEntity.ok(metricsService.getLastHealthCheck());
    }

    @GetMapping("/up-count")
    public ResponseEntity<Long> getUpCount() {
        return ResponseEntity.ok(metricsService.getUpCount());
    }

    @GetMapping("/down-count")
    public ResponseEntity<Long> getDownCount() {
        return ResponseEntity.ok(metricsService.getDownCount());
    }

    @GetMapping("/failed-count")
    public ResponseEntity<Long> getFailedChecksCount() {
        return ResponseEntity.ok(metricsService.getFailedChecksCount());
    }

    @GetMapping("/slow-count")
    public ResponseEntity<Long> getSlowApisCount() {
        return ResponseEntity.ok(metricsService.getSlowApisCount());
    }
}
