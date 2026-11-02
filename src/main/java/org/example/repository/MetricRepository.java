package org.example.repository;

import org.example.entity.Endpoint;
import org.example.entity.Metrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MetricRepository extends JpaRepository<Metrics, Long> {
    List<Metrics> findByEndpointId(Long endpointId);
    List<Metrics> findTop10ByEndpointIdOrderByTimeStampDesc(Long endpointId);
    List<Metrics> findByEndpointIdOrderByTimeStampDesc(Long endpointId);
    List<Metrics> findByResponseTimeGreaterThan(double responseTime);
    List<Metrics> findByStatusCode(int statusCode);
    List<Metrics> findTop10ByOrderByTimeStampDesc();
    List<Metrics> findBySuccessFalse();
    List<Metrics> findBySuccessTrue();
    long countByEndpointId(Long endpointId);
    List<Metrics> findByStatusCodeGreaterThanEqual(int statusCode);
    long countBySuccessFalse();
    Optional<Metrics>findTopByEndpoint_IdOrderByTimeStampDesc(Long endpointId);
    @Modifying
    @Transactional
    @Query("DELETE FROM Metrics m WHERE m.endpoint = :endpoint")
    void deleteByEndpoint(@Param("endpoint") Endpoint endpoint);

    @Modifying
    @Transactional
    @Query("DELETE FROM Metrics m WHERE m.endpoint.id = :endpointId")
    void deleteByEndpointId(@Param("endpointId") Long endpointId);

    @Query("SELECT AVG(m.responseTime) FROM Metrics m WHERE m.success=true")
    Double getAverageResponseTime();

    @Query("SELECT MAX(m.timeStamp) FROM Metrics m")
    LocalDateTime getLastHealthCheck();

    @Query("""
        SELECT COUNT(m)
        FROM Metrics m
        WHERE m.status = :status
        AND NOT EXISTS (
            SELECT m2.id
            FROM Metrics m2
            WHERE m2.endpoint.id = m.endpoint.id
            AND m2.timeStamp > m.timeStamp
        )
    """)
    long countLatestByStatus(@Param("status") Metrics.HealthStatus status);

    @Query("""
        SELECT COUNT(m)
        FROM Metrics m
        WHERE m.responseTime >= :threshold
        AND NOT EXISTS (
            SELECT m2.id
            FROM Metrics m2
            WHERE m2.endpoint.id = m.endpoint.id
            AND m2.timeStamp > m.timeStamp
        )
    """)
    long countLatestSlowApis(@Param("threshold") double threshold);
}
