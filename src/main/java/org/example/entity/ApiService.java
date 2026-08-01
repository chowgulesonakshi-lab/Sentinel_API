package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "apiService")
public class ApiService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;

    @ManyToOne
    @JoinColumn(name="project_id")
    private Project project;

    @Enumerated(EnumType.STRING)
    private ServiceStatus status;

    public enum ServiceStatus {
        ACTIVE,
        INACTIVE,
        MAINTENANCE
    }
}
