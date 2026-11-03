package org.example.service;

import org.example.dto.DependencyImpactResponseDTO;
import org.example.dto.ServiceDependencyRequestDTO;
import org.example.entity.ApiService;
import org.example.entity.Project;
import org.example.entity.ServiceDependency;
import org.example.repository.ApiServiceRepository;
import org.example.repository.EndpointRepository;
import org.example.repository.MetricRepository;
import org.example.repository.ProjectRepository;
import org.example.repository.ServiceDependencyRepository;
import org.example.serviceImplDb.ServiceDependencyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceDependencyServiceTest {
    @Mock private ServiceDependencyRepository dependencyRepository;
    @Mock private ApiServiceRepository serviceRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private EndpointRepository endpointRepository;
    @Mock private MetricRepository metricRepository;

    private ServiceDependencyServiceImpl service;
    private Project project;
    private ApiService a;
    private ApiService b;
    private ApiService c;

    @BeforeEach
    void setUp() {
        service = new ServiceDependencyServiceImpl(dependencyRepository, serviceRepository, projectRepository, endpointRepository, metricRepository);
        project = project(1L);
        a = service(10L, "Order", project);
        b = service(20L, "Payment", project);
        c = service(30L, "Inventory", project);
    }

    @Test
    void rejectsSelfAndCrossProjectDependencies() {
        when(serviceRepository.findById(10L)).thenReturn(Optional.of(a));
        ServiceDependencyRequestDTO self = request(10L, 10L);
        assertThrows(IllegalArgumentException.class, () -> service.create(self));

        Project otherProject = project(2L);
        ApiService other = service(40L, "Other", otherProject);
        when(serviceRepository.findById(40L)).thenReturn(Optional.of(other));
        assertThrows(IllegalArgumentException.class, () -> service.create(request(10L, 40L)));
    }

    @Test
    void rejectsDuplicateAndCircularRelationships() {
        when(serviceRepository.findById(10L)).thenReturn(Optional.of(a));
        when(serviceRepository.findById(20L)).thenReturn(Optional.of(b));
        when(dependencyRepository.existsByDependentServiceIdAndDependencyServiceId(10L, 20L)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.create(request(10L, 20L)));

        when(dependencyRepository.existsByDependentServiceIdAndDependencyServiceId(20L, 10L)).thenReturn(false);
        ServiceDependency existing = relationship(10L, 20L, a, b);
        when(dependencyRepository.findByDependentServiceId(10L)).thenReturn(List.of(existing));
        assertThrows(IllegalArgumentException.class, () -> service.create(request(20L, 10L)));
    }

    @Test
    void identifiesDirectAndIndirectPotentialImpact() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(serviceRepository.findById(10L)).thenReturn(Optional.of(a));
        when(serviceRepository.findById(20L)).thenReturn(Optional.of(b));
        when(serviceRepository.findById(30L)).thenReturn(Optional.of(c));
        ServiceDependency bOnA = relationship(1L, 20L, b, a);
        ServiceDependency cOnB = relationship(2L, 30L, c, b);
        when(dependencyRepository.findByDependencyServiceId(10L)).thenReturn(List.of(bOnA));
        when(dependencyRepository.findByDependencyServiceId(20L)).thenReturn(List.of(cOnB));
        when(endpointRepository.findByApiServiceId(any())).thenReturn(List.of());

        DependencyImpactResponseDTO impact = service.impact(1L, 10L);

        assertEquals(List.of(20L), impact.getDirectDependentServiceIds());
        assertEquals(List.of(30L), impact.getIndirectDependentServiceIds());
        assertEquals(List.of(20L, 30L), impact.getPotentiallyAffectedServiceIds());
        assertTrue(impact.getConfirmedUnhealthyServiceIds().isEmpty());
    }

    private Project project(Long id) {
        Project project = new Project();
        project.setId(id);
        return project;
    }

    private ApiService service(Long id, String name, Project project) {
        ApiService service = new ApiService();
        service.setId(id);
        service.setName(name);
        service.setProject(project);
        return service;
    }

    private ServiceDependencyRequestDTO request(Long dependent, Long dependency) {
        ServiceDependencyRequestDTO request = new ServiceDependencyRequestDTO();
        request.setDependentServiceId(dependent);
        request.setDependencyServiceId(dependency);
        return request;
    }

    private ServiceDependency relationship(Long id, Long ignored, ApiService dependent, ApiService dependency) {
        ServiceDependency relationship = new ServiceDependency();
        relationship.setId(id);
        relationship.setProject(project);
        relationship.setDependentService(dependent);
        relationship.setDependencyService(dependency);
        return relationship;
    }
}
