package org.example.repository;

import org.example.entity.Endpoint;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EndpointRepository extends JpaRepository<Endpoint, Long> {
    @Override
    @EntityGraph(attributePaths = {"apiService", "apiService.project", "apiService.project.owner"})
    Optional<Endpoint> findById(Long id);
    Optional<Endpoint> findByIdAndApiService_Project_Owner_EmailIgnoreCase(Long id, String email);
    List<Endpoint> findByMethod(Endpoint.HttpMethod method);
    List<Endpoint> findByTimeoutMillisGreaterThan(Integer timeoutMillis);
    List<Endpoint> findByApiServiceId(Long serviceId);
    @EntityGraph(attributePaths = {"apiService", "apiService.project", "apiService.project.owner"})
    List<Endpoint> findByEnabledTrue();
    List<Endpoint> findByEnabledFalse();
}
