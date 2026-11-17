package org.example.repository;
import org.example.entity.Incident; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface IncidentRepository extends JpaRepository<Incident,Long>{
 Optional<Incident> findByIdAndEndpoint_ApiService_Project_Owner_EmailIgnoreCase(Long id,String email);
 List<Incident> findByEndpoint_ApiService_Project_Owner_EmailIgnoreCaseOrderByCreatedAtDesc(String email);
 List<Incident> findByEndpoint_IdAndEndpoint_ApiService_Project_Owner_EmailIgnoreCaseOrderByCreatedAtDesc(Long endpointId,String email);
}