package org.example.repository;

import org.example.entity.ServiceDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ServiceDependencyRepository extends JpaRepository<ServiceDependency, Long> {
    List<ServiceDependency> findByProjectId(Long projectId);
    List<ServiceDependency> findByDependentServiceId(Long serviceId);
    List<ServiceDependency> findByDependencyServiceId(Long serviceId);
    boolean existsByDependentServiceIdAndDependencyServiceId(Long dependentServiceId, Long dependencyServiceId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ServiceDependency d WHERE d.dependentService.id = :serviceId OR d.dependencyService.id = :serviceId")
    void deleteByServiceId(@Param("serviceId") Long serviceId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ServiceDependency d WHERE d.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);
}
