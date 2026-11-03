package org.example.serviceImplDb;

import lombok.RequiredArgsConstructor;
import org.example.dto.DependencyImpactResponseDTO;
import org.example.dto.ServiceDependencyGraphResponseDTO;
import org.example.dto.ServiceDependencyRequestDTO;
import org.example.dto.ServiceDependencyResponseDTO;
import org.example.entity.ApiService;
import org.example.entity.Endpoint;
import org.example.entity.Metrics;
import org.example.entity.Project;
import org.example.entity.ServiceDependency;
import org.example.repository.ApiServiceRepository;
import org.example.repository.EndpointRepository;
import org.example.repository.MetricRepository;
import org.example.repository.ProjectRepository;
import org.example.repository.ServiceDependencyRepository;
import org.example.service.ServiceDependencyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class ServiceDependencyServiceImpl implements ServiceDependencyService {
    private final ServiceDependencyRepository dependencyRepository;
    private final ApiServiceRepository serviceRepository;
    private final ProjectRepository projectRepository;
    private final EndpointRepository endpointRepository;
    private final MetricRepository metricRepository;

    @Override
    public ServiceDependencyResponseDTO create(ServiceDependencyRequestDTO request) {
        ApiService dependent = service(request.getDependentServiceId());
        ApiService dependency = service(request.getDependencyServiceId());
        validateRelationship(dependent, dependency, null);

        ServiceDependency relationship = new ServiceDependency();
        relationship.setProject(dependent.getProject());
        relationship.setDependentService(dependent);
        relationship.setDependencyService(dependency);
        return map(dependencyRepository.save(relationship));
    }

    @Override
    public ServiceDependencyResponseDTO update(Long id, ServiceDependencyRequestDTO request) {
        ServiceDependency relationship = dependencyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dependency not found: " + id));
        ApiService dependent = service(request.getDependentServiceId());
        ApiService dependency = service(request.getDependencyServiceId());
        validateRelationship(dependent, dependency, id);
        relationship.setProject(dependent.getProject());
        relationship.setDependentService(dependent);
        relationship.setDependencyService(dependency);
        return map(dependencyRepository.save(relationship));
    }

    @Override
    public void delete(Long id) {
        if (!dependencyRepository.existsById(id)) {
            throw new IllegalArgumentException("Dependency not found: " + id);
        }
        dependencyRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceDependencyResponseDTO> listByProject(Long projectId) {
        requireProject(projectId);
        return dependencyRepository.findByProjectId(projectId).stream().map(this::map).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceDependencyResponseDTO> listForService(Long serviceId) {
        service(serviceId);
        return dependencyRepository.findByDependentServiceId(serviceId).stream().map(this::map).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceDependencyGraphResponseDTO graph(Long projectId) {
        requireProject(projectId);
        List<ServiceDependencyGraphResponseDTO.ServiceNode> nodes = serviceRepository.findByProjectId(projectId).stream()
                .map(service -> ServiceDependencyGraphResponseDTO.ServiceNode.builder()
                        .id(service.getId())
                        .name(service.getName())
                        .status(service.getStatus() == null ? "UNKNOWN" : service.getStatus().name())
                        .health(health(service))
                        .build())
                .toList();
        List<ServiceDependencyGraphResponseDTO.ServiceEdge> edges = dependencyRepository.findByProjectId(projectId).stream()
                .map(edge -> ServiceDependencyGraphResponseDTO.ServiceEdge.builder()
                        .id(edge.getId())
                        .dependentServiceId(edge.getDependentService().getId())
                        .dependencyServiceId(edge.getDependencyService().getId())
                        .build())
                .toList();
        return ServiceDependencyGraphResponseDTO.builder().projectId(projectId).nodes(nodes).edges(edges).build();
    }

    @Override
    @Transactional(readOnly = true)
    public DependencyImpactResponseDTO impact(Long projectId, Long serviceId) {
        Project project = requireProject(projectId);
        ApiService root = service(serviceId);
        if (!root.getProject().getId().equals(project.getId())) {
            throw new IllegalArgumentException("Service does not belong to project");
        }

        Set<Long> direct = dependencyRepository.findByDependencyServiceId(serviceId).stream()
                .map(d -> d.getDependentService().getId()).collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        Set<Long> all = new LinkedHashSet<>(direct);
        Queue<Long> queue = new ArrayDeque<>(direct);
        while (!queue.isEmpty()) {
            Long current = queue.remove();
            for (ServiceDependency relationship : dependencyRepository.findByDependencyServiceId(current)) {
                Long dependent = relationship.getDependentService().getId();
                if (all.add(dependent)) queue.add(dependent);
            }
        }
        Set<Long> confirmed = new LinkedHashSet<>();
        for (Long affectedId : all) {
            if ("UNHEALTHY".equals(health(service(affectedId)))) confirmed.add(affectedId);
        }
        Set<Long> indirect = new LinkedHashSet<>(all);
        indirect.removeAll(direct);
        Set<Long> potential = new LinkedHashSet<>(all);
        potential.removeAll(confirmed);
        return DependencyImpactResponseDTO.builder()
                .rootServiceId(serviceId)
                .directDependentServiceIds(new ArrayList<>(direct))
                .indirectDependentServiceIds(new ArrayList<>(indirect))
                .confirmedUnhealthyServiceIds(new ArrayList<>(confirmed))
                .potentiallyAffectedServiceIds(new ArrayList<>(potential))
                .build();
    }

    private void validateRelationship(ApiService dependent, ApiService dependency, Long existingId) {
        if (dependent.getId().equals(dependency.getId())) {
            throw new IllegalArgumentException("A service cannot depend on itself");
        }
        if (!dependent.getProject().getId().equals(dependency.getProject().getId())) {
            throw new IllegalArgumentException("Both services must belong to the same project");
        }
        if (dependencyRepository.existsByDependentServiceIdAndDependencyServiceId(dependent.getId(), dependency.getId())
                && (existingId == null || !dependencyRepository.findById(existingId).map(existing -> existing.getDependentService().getId().equals(dependent.getId()) && existing.getDependencyService().getId().equals(dependency.getId())).orElse(false))) {
            throw new IllegalArgumentException("This dependency already exists");
        }
        if (pathExists(dependency.getId(), dependent.getId(), existingId)) {
            throw new IllegalArgumentException("The dependency would create a circular relationship");
        }
    }

    private boolean pathExists(Long from, Long target, Long ignoredId) {
        Set<Long> visited = new HashSet<>();
        Queue<Long> queue = new ArrayDeque<>();
        queue.add(from);
        while (!queue.isEmpty()) {
            Long current = queue.remove();
            if (!visited.add(current)) continue;
            if (current.equals(target)) return true;
            dependencyRepository.findByDependentServiceId(current).stream()
                    .filter(edge -> ignoredId == null || !edge.getId().equals(ignoredId))
                    .map(edge -> edge.getDependencyService().getId())
                    .forEach(queue::add);
        }
        return false;
    }

    private String health(ApiService service) {
        List<Endpoint> endpoints = endpointRepository.findByApiServiceId(service.getId());
        if (endpoints.isEmpty()) return "UNKNOWN";
        boolean hasLatest = false;
        for (Endpoint endpoint : endpoints) {
            Metrics latest = metricRepository.findTopByEndpoint_IdOrderByTimeStampDesc(endpoint.getId()).orElse(null);
            if (latest != null) {
                hasLatest = true;
                if (latest.getStatus() == Metrics.HealthStatus.DOWN || !latest.isSuccess()) return "UNHEALTHY";
            }
        }
        return hasLatest ? "HEALTHY" : "UNKNOWN";
    }

    private ApiService service(Long id) {
        return serviceRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Service not found: " + id));
    }

    private Project requireProject(Long id) {
        return projectRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Project not found: " + id));
    }

    private ServiceDependencyResponseDTO map(ServiceDependency relationship) {
        ServiceDependencyResponseDTO dto = new ServiceDependencyResponseDTO();
        dto.setId(relationship.getId());
        dto.setProjectId(relationship.getProject().getId());
        dto.setDependentServiceId(relationship.getDependentService().getId());
        dto.setDependentServiceName(relationship.getDependentService().getName());
        dto.setDependencyServiceId(relationship.getDependencyService().getId());
        dto.setDependencyServiceName(relationship.getDependencyService().getName());
        dto.setCreatedAt(relationship.getCreatedAt());
        return dto;
    }
}
