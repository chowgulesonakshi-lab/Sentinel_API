package org.example.repository;

import org.example.entity.Endpoint;
import org.example.entity.Alerts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlertsRepository extends JpaRepository<Alerts, Long> {
    List<Alerts> findByResolvedFalse();
    List<Alerts> findByResolvedTrue();
    List<Alerts> findByEndpointId(Long endpointId);
    List<Alerts> findBySeverity(Alerts.Severity severity);
    List<Alerts> findTop10ByOrderByCreatedAtDesc();
    Optional<Alerts> findByEndpointIdAndTypeAndResolvedFalse(Long endpointId, Alerts.AlertType type);
    long countByResolvedFalse();
    long countBySeverity(Alerts.Severity severity);
    void deleteByEndpoint(Endpoint endpoint);
}
