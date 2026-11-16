package org.example.repository;

import org.example.entity.AlertRule;
import org.example.entity.Endpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AlertRuleRepository extends JpaRepository<AlertRule, Long> {
    List<AlertRule> findByEnabledTrue();
    List<AlertRule> findByEndpointId(Long endpointId);
    List<AlertRule> findByApiServiceId(Long apiServiceId);

    @Query("""
        SELECT r FROM AlertRule r
        WHERE r.enabled = true
        AND (
            r.endpoint.id = :endpointId
            OR (r.endpoint IS NULL AND r.apiService.id = :serviceId)
            OR (r.endpoint IS NULL AND r.apiService IS NULL)
        )
    """)
    List<AlertRule> findApplicableRules(@Param("endpointId") Long endpointId, @Param("serviceId") Long serviceId);

    @Modifying
    @Transactional
    @Query("DELETE FROM AlertRule r WHERE r.endpoint = :endpoint")
    void deleteByEndpoint(@Param("endpoint") Endpoint endpoint);
}
