package org.example.repository;

import org.example.entity.Endpoint;
import org.example.entity.Alerts;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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

    @Modifying
    @Transactional
    @Query("DELETE FROM Alerts a WHERE a.endpoint = :endpoint")
    void deleteByEndpoint(@Param("endpoint") Endpoint endpoint);

    @Modifying
    @Transactional
    @Query("DELETE FROM Alerts a WHERE a.endpoint.id = :endpointId")
    void deleteByEndpointId(@Param("endpointId") Long endpointId);
}
