package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "endpoints")
public class Endpoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private HttpMethod method;

    private String url;
    private Integer timeoutMillis;
    private boolean enabled;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private ApiService apiService;

    public enum HttpMethod {
        GET,
        POST,
        PUT,
        DELETE,
        PATCH
    }
}
