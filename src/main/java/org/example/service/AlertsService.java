package org.example.service;

import org.example.dto.AlertsResponseDTO;
import org.example.entity.Alerts;

import java.util.List;
import java.util.Optional;

public interface AlertsService {
    Alerts saveAlert(Alerts alert);
    List<AlertsResponseDTO> getAllAlerts();
    Optional<AlertsResponseDTO> getAlertById(Long id);
    List<AlertsResponseDTO> getActiveAlerts();
    List<AlertsResponseDTO> getResolvedAlerts();
    List<AlertsResponseDTO> getAlertsByEndpoint(Long endpointId);
    List<AlertsResponseDTO> getAlertsBySeverity(Alerts.Severity severity);
    List<AlertsResponseDTO> getRecentAlerts();
    long getActiveAlertCount();
    long getCriticalAlertCount();
    Optional<Alerts> getActiveAlert(Long endpointId, Alerts.AlertType type);
    AlertsResponseDTO resolveAlert(Long alertId);
}