package org.example.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ServiceDependencyGraphResponseDTO {
    Long projectId;
    List<ServiceNode> nodes;
    List<ServiceEdge> edges;

    @Value
    @Builder
    public static class ServiceNode {
        Long id;
        String name;
        String status;
        String health;
    }

    @Value
    @Builder
    public static class ServiceEdge {
        Long id;
        Long dependentServiceId;
        Long dependencyServiceId;
    }
}
