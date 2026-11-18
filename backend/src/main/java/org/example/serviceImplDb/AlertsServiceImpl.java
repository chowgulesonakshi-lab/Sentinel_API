package org.example.serviceImplDb;

import org.example.dto.AlertsResponseDTO;
import org.example.dto.AlertWebhookPayload;
import org.example.notification.AlertWebhookNotifier;
import org.example.entity.Alerts;
import org.example.repository.AlertsRepository;
import org.example.service.AlertsService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Service
public class AlertsServiceImpl implements AlertsService {
    private final AlertsRepository alertsRepository;
    private final AlertWebhookNotifier alertWebhookNotifier;

    @Autowired
    public AlertsServiceImpl(AlertsRepository alertsRepository, AlertWebhookNotifier alertWebhookNotifier) {
        this.alertsRepository = alertsRepository;
        this.alertWebhookNotifier = alertWebhookNotifier;
    }

    public AlertsServiceImpl(AlertsRepository alertsRepository) {
        this(alertsRepository, null);
    }

    private void notifyWebhook(Alerts alert, String event) {
        if (alertWebhookNotifier == null) {
            return;
        }
        AlertWebhookPayload payload = new AlertWebhookPayload(
                alert.getId(),
                event,
                alert.getType() == null ? "UNKNOWN" : alert.getType().name(),
                alert.getSeverity() == null ? "UNKNOWN" : alert.getSeverity().name(),
                alert.getMessage(),
                alert.isResolved(),
                alert.getResolvedAt() != null ? alert.getResolvedAt()
                        : (alert.getCreatedAt() != null ? alert.getCreatedAt() : LocalDateTime.now())
        );
        try {
            alertWebhookNotifier.send(payload);
        } catch (RuntimeException exception) {
            // Executor saturation must not prevent the alert from being saved.
            org.slf4j.LoggerFactory.getLogger(AlertsServiceImpl.class)
                    .warn("Unable to queue alert webhook {}: {}", alert.getId(), exception.getMessage());
        }
    }

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
        Alerts saved = alertsRepository.save(alert);
        notifyWebhook(saved, saved.isResolved() ? "ALERT_RESOLVED" : "ALERT_CREATED");
        return saved;
    }

    @Override
    public List<AlertsResponseDTO> getAllAlerts() {
        return alertsRepository.findAll()
                .stream()
                .filter(org.example.security.ResourceOwnership::isCurrentUser)
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public Optional<AlertsResponseDTO> getAlertById(Long id) {
        return alertsRepository.findById(id)
                .filter(org.example.security.ResourceOwnership::isCurrentUser)
                .map(this::mapToDTO);
    }

    @Override
    public List<AlertsResponseDTO> getActiveAlerts() {
        return alertsRepository.findByResolvedFalse()
                .stream()
                .filter(org.example.security.ResourceOwnership::isCurrentUser)
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<AlertsResponseDTO> getResolvedAlerts() {
        return alertsRepository.findByResolvedTrue()
                .stream()
                .filter(org.example.security.ResourceOwnership::isCurrentUser)
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<AlertsResponseDTO> getAlertsByEndpoint(Long endpointId) {
        return alertsRepository.findByEndpointId(endpointId)
                .stream()
                .filter(org.example.security.ResourceOwnership::isCurrentUser)
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<AlertsResponseDTO> getAlertsBySeverity(Alerts.Severity severity) {
        return alertsRepository.findBySeverity(severity)
                .stream()
                .filter(org.example.security.ResourceOwnership::isCurrentUser)
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<AlertsResponseDTO> getRecentAlerts() {
        return alertsRepository.findTop10ByOrderByCreatedAtDesc()
                .stream()
                .filter(org.example.security.ResourceOwnership::isCurrentUser)
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public long getActiveAlertCount() {
        return alertsRepository.findByResolvedFalse().stream()
                .filter(org.example.security.ResourceOwnership::isCurrentUser).count();
    }

    @Override
    public long getCriticalAlertCount() {
        return alertsRepository.findBySeverity(Alerts.Severity.CRITICAL).stream()
                .filter(org.example.security.ResourceOwnership::isCurrentUser).count();
    }

    @Override
    public Optional<Alerts> getActiveAlert(Long endpointId, Alerts.AlertType type) {
        return alertsRepository.findByEndpointIdAndTypeAndResolvedFalse(endpointId, type);
    }

    @Override
    public AlertsResponseDTO resolveAlert(Long alertId) {
        Alerts alert = alertsRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));
        org.example.security.ResourceOwnership.requireOwner(alert);
        alert.setResolved(true);
        alert.setResolvedAt(java.time.LocalDateTime.now());
        Alerts saved = alertsRepository.save(alert);
        notifyWebhook(saved, "ALERT_RESOLVED");
        return mapToDTO(saved);
    }
}
