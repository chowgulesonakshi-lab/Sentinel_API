package org.example.repository;
import org.example.entity.IncidentEvent; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface IncidentEventRepository extends JpaRepository<IncidentEvent,Long>{List<IncidentEvent> findByIncident_IdOrderByCreatedAtAsc(Long id);}