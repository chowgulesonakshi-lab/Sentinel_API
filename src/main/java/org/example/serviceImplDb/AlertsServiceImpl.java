package org.example.serviceImplDb;

import lombok.RequiredArgsConstructor;
import org.example.dto.AlertsResponseDTO;
import org.example.entity.Alerts;
import org.example.repository.AlertsRepository;
import org.example.service.AlertsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlertsServiceImpl implements AlertsService {
    private final AlertsRepository alertsRepository;

    private AlertsResponseDTO mapToDTO(Alerts alert) {
        AlertsResponseDTO dto = new AlertsResponseDTO();
        dto.setId(alert.getId());

        dto.setType(alert.getType());
        dto.setSeverity(alert.getSeverity());
        dto.setMessage(alert.getMessage());

        dto.setResolved(alert.isResolved());

        dto.setCreatedAt(alert.getCreatedAt());
        dto.setResolvedAt(alert.getResolvedAt());
        dto.setAnomalyObservedResponseTime(alert.getAnomalyObservedResponseTime());
        dto.setAnomalyHistoricalAverage(alert.getAnomalyHistoricalAverage());
        dto.setAnomalyStandardDeviation(alert.getAnomalyStandardDeviation());
        dto.setAnomalyDetectionThreshold(alert.getAnomalyDetectionThreshold());
        dto.setAnomalyDeviationFromAverage(alert.getAnomalyDeviationFromAverage());
        dto.setAnomalySensitivityMultiplier(alert.getAnomalySensitivityMultiplier());
        dto.setAnomalyExplanation(alert.getAnomalyExplanation());

        if (alert.getEndpoint() != null) {
            dto.setEndpointId(alert.getEndpoint().getId());
            dto.setEndpointUrl(alert.getEndpoint().getUrl());
        }
        if (alert.getRule() != null) {
            dto.setRuleId(alert.getRule().getId());
            dto.setRuleName(alert.getRule().getName());
        }
        return dto;
    }

    @Override
    public Alerts saveAlert(Alerts alert) {
        return alertsRepository.save(alert);
    }

    @Override
    public List<AlertsResponseDTO> getAllAlerts() {
        return alertsRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public Optional<AlertsResponseDTO> getAlertById(Long id) {
        return alertsRepository.findById(id)
                .map(this::mapToDTO);
    }

    @Override
    public List<AlertsResponseDTO> getActiveAlerts() {
        return alertsRepository.findByResolvedFalse()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<AlertsResponseDTO> getResolvedAlerts() {
        return alertsRepository.findByResolvedTrue()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<AlertsResponseDTO> getAlertsByEndpoint(Long endpointId) {
        return alertsRepository.findByEndpointId(endpointId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<AlertsResponseDTO> getAlertsBySeverity(Alerts.Severity severity) {
        return alertsRepository.findBySeverity(severity)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<AlertsResponseDTO> getRecentAlerts() {
        return alertsRepository.findTop10ByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public long getActiveAlertCount() {
        return alertsRepository.countByResolvedFalse();
    }

    @Override
    public long getCriticalAlertCount() {
        return alertsRepository.countBySeverity(Alerts.Severity.CRITICAL);
    }

    @Override
    public Optional<Alerts> getActiveAlert(Long endpointId, Alerts.AlertType type) {
        return alertsRepository.findByEndpointIdAndTypeAndResolvedFalse(endpointId, type);
    }

    @Override
    public AlertsResponseDTO resolveAlert(Long alertId) {
        Alerts alert = alertsRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));
        alert.setResolved(true);
        alert.setResolvedAt(java.time.LocalDateTime.now());
        Alerts saved = alertsRepository.save(alert);
        return mapToDTO(saved);
    }
}
