package org.example.entity;
import jakarta.persistence.*; import lombok.Data; import java.time.LocalDateTime;
@Data @Entity @Table(name="incident_events")
public class IncidentEvent {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="incident_id",nullable=false) private Incident incident;
 @Column(name="actor_email",nullable=false) private String actorEmail; @Column(name="event_type",nullable=false) private String eventType;
 @Column(columnDefinition="TEXT") private String details; @Column(name="created_at",nullable=false) private LocalDateTime createdAt;
 @PrePersist void create(){if(createdAt==null)createdAt=LocalDateTime.now();}
}