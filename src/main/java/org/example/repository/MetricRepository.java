package org.example.repository;

import org.example.entity.Endpoint;
import org.example.entity.Metrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MetricRepository extends JpaRepository<Metrics, Long> {
    List<Metrics> findByEndpointId(Long endpointId);
    List<Metrics> findTop10ByEndpointIdOrderByTimeStampDesc(Long endpointId);
    List<Metrics> findByResponseTimeGreaterThan(double responseTime);
    List<Metrics> findByStatusCode(int statusCode);
    List<Metrics> findTop10ByOrderByTimeStampDesc();
    long countByStatus(Metrics.HealthStatus status);
    List<Metrics> findBySuccessFalse();
    List<Metrics> findBySuccessTrue();
    long countByEndpointId(Long endpointId);
    List<Metrics> findByStatusCodeGreaterThanEqual(int statusCode);
    long countBySuccessFalse();
    Optional<Metrics>findTopByEndpoint_IdOrderByTimeStampDesc(Long endpointId);
    void deleteByEndpoint(Endpoint endpoint);

    @Query("SELECT AVG(m.responseTime) FROM Metrics m WHERE m.success=true")
    Double getAverageResponseTime();

    @Query("SELECT MAX(m.timeStamp) FROM Metrics m")
    LocalDateTime getLastHealthCheck();
}