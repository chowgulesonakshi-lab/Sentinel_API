package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "metrics")
public class Metrics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double responseTime;
    private boolean success;
    private Integer statusCode;
    private LocalDateTime timeStamp;

    @ManyToOne
    @JoinColumn(name = "endpointId")
    private Endpoint endpoint;

    @Enumerated(EnumType.STRING)
    private HealthStatus status;

    public enum HealthStatus {
        UP,
        DOWN
    }
}
