package org.example.repository;
import java.util.List;
import java.util.Optional;
import org.example.entity.ApiService;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiServiceRepository extends JpaRepository<ApiService, Long> {
    List<ApiService>  findByName(String name);
    List<ApiService> findByStatus(ApiService.ServiceStatus status);
    List<ApiService> findByProjectId(Long projectId);
    long countByStatus(ApiService.ServiceStatus status);
}
