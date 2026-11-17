package org.example.repository;

import org.example.entity.SloConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SloConfigurationRepository extends JpaRepository<SloConfiguration, Long> {
    Optional<SloConfiguration> findByEndpoint_IdAndEndpoint_ApiService_Project_Owner_EmailIgnoreCase(
            Long endpointId, String email);
}
