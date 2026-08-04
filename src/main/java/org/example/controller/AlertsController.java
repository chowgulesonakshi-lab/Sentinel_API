package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.AlertsResponseDTO;
import org.example.entity.Alerts;
import org.example.service.AlertsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alerts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AlertsController {

    private final AlertsService alertsService;

    @GetMapping
    public ResponseEntity<List<AlertsResponseDTO>> getAllAlerts() {
        return ResponseEntity.ok(alertsService.getAllAlerts());
    }

    @GetMapping("/active")
    public ResponseEntity<List<AlertsResponseDTO>> getActiveAlerts() {
        return ResponseEntity.ok(alertsService.getActiveAlerts());
    }

    @GetMapping("/resolved")
    public ResponseEntity<List<AlertsResponseDTO>> getResolvedAlerts() {
        return ResponseEntity.ok(alertsService.getResolvedAlerts());
    }

    @GetMapping("/recent")
    public ResponseEntity<List<AlertsResponseDTO>> getRecentAlerts() {
        return ResponseEntity.ok(alertsService.getRecentAlerts());
    }

    @GetMapping("/active-count")
    public ResponseEntity<Long> getActiveAlertCount() {
        return ResponseEntity.ok(alertsService.getActiveAlertCount());
    }

    @GetMapping("/critical-count")
    public ResponseEntity<Long> getCriticalAlertCount() {
        return ResponseEntity.ok(alertsService.getCriticalAlertCount());
    }

    @GetMapping("/endpoint/{endpointId}")
    public ResponseEntity<List<AlertsResponseDTO>> getAlertsByEndpoint(
            @PathVariable Long endpointId) {

        return ResponseEntity.ok(alertsService.getAlertsByEndpoint(endpointId));
    }

    @GetMapping("/severity/{severity}")
    public ResponseEntity<List<AlertsResponseDTO>> getAlertsBySeverity(
            @PathVariable Alerts.Severity severity) {

        return ResponseEntity.ok(alertsService.getAlertsBySeverity(severity));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertsResponseDTO> getAlertById(
            @PathVariable Long id) {

        return alertsService.getAlertById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}