package org.example.entity;
import jakarta.persistence.*; import lombok.Data; import java.time.LocalDateTime;
@Data @Entity @Table(name="incidents")
public class Incident {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="endpoint_id",nullable=false) private Endpoint endpoint;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="alert_id") private Alerts sourceAlert;
 @Column(nullable=false,length=180) private String title; @Column(columnDefinition="TEXT") private String description;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private Status status=Status.OPEN;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private Severity severity=Severity.MEDIUM;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private Priority priority=Priority.P2;
 @Column(name="assigned_to") private String assignedTo; @Column(name="created_by",nullable=false) private String createdBy;
 @Column(name="created_at",nullable=false) private LocalDateTime createdAt; @Column(name="acknowledged_at") private LocalDateTime acknowledgedAt;
 @Column(name="investigating_at") private LocalDateTime investigatingAt; @Column(name="resolved_at") private LocalDateTime resolvedAt;
 @Column(name="updated_at",nullable=false) private LocalDateTime updatedAt;
 public enum Status{OPEN,ACKNOWLEDGED,INVESTIGATING,RESOLVED} public enum Severity{CRITICAL,HIGH,MEDIUM,LOW} public enum Priority{P1,P2,P3,P4}
 @PrePersist void create(){LocalDateTime n=LocalDateTime.now();if(createdAt==null)createdAt=n;if(updatedAt==null)updatedAt=n;}
 @PreUpdate void update(){updatedAt=LocalDateTime.now();}
}