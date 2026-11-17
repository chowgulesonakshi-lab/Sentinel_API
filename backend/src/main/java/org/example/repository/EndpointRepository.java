package org.example.repository;

import org.example.entity.Endpoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EndpointRepository extends JpaRepository<Endpoint, Long> {
    Optional<Endpoint> findByIdAndApiService_Project_Owner_EmailIgnoreCase(Long id, String email);
    List<Endpoint> findByMethod(Endpoint.HttpMethod method);
    List<Endpoint> findByTimeoutMillisGreaterThan(Integer timeoutMillis);
    List<Endpoint> findByApiServiceId(Long serviceId);
    List<Endpoint> findByEnabledTrue();
    List<Endpoint> findByEnabledFalse();
}
