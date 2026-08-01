package org.example.repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.example.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByCreatedAt(LocalDate date);
    List<Project> findByOwnerId(Long ownerId);
    Optional<Project> findByName(String name);
}
