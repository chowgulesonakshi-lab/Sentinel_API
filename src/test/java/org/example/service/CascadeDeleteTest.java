package org.example.service;

import org.example.entity.Alerts;
import org.example.entity.ApiService;
import org.example.entity.Endpoint;
import org.example.entity.Metrics;
import org.example.entity.Project;
import org.example.repository.AlertsRepository;
import org.example.repository.ApiServiceRepository;
import org.example.repository.AppUserRepository;
import org.example.repository.EndpointRepository;
import org.example.repository.MetricRepository;
import org.example.repository.ProjectRepository;
import org.example.serviceImplDb.ApiServiceImplDb;
import org.example.serviceImplDb.EndpointServiceImplDb;
import org.example.serviceImplDb.ProjectServiceImplDb;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CascadeDeleteTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private ApiServiceRepository apiServiceRepository;

    @Mock
    private ApiService_Service apiServiceService;

    @Mock
    private EndpointRepository endpointRepository;

    @Mock
    private EndpointService endpointService;

    @Mock
    private AlertsRepository alertsRepository;

    @Mock
    private MetricRepository metricRepository;

    @InjectMocks
    private ProjectServiceImplDb projectServiceImpl;

    @InjectMocks
    private ApiServiceImplDb apiServiceImpl;

    @InjectMocks
    private EndpointServiceImplDb endpointServiceImpl;

    @Test
    void testDeleteProjectCascadesToServicesAndDeletesProject() {
        Long projectId = 1L;
        Project project = new Project();
        project.setId(projectId);

        ApiService service1 = new ApiService();
        service1.setId(10L);
        ApiService service2 = new ApiService();
        service2.setId(20L);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(apiServiceRepository.findByProjectId(projectId)).thenReturn(List.of(service1, service2));

        projectServiceImpl.deleteProject(projectId);

        // Verify that deleteService was called for every associated service
        verify(apiServiceService, times(1)).deleteService(10L);
        verify(apiServiceService, times(1)).deleteService(20L);

        // Verify project itself was deleted
        verify(projectRepository, times(1)).delete(project);
    }

    @Test
    void testDeleteServiceCascadesToEndpointsAndDeletesService() {
        Long serviceId = 10L;
        ApiService service = new ApiService();
        service.setId(serviceId);

        Endpoint endpoint1 = new Endpoint();
        endpoint1.setId(100L);
        Endpoint endpoint2 = new Endpoint();
        endpoint2.setId(200L);

        when(apiServiceRepository.findById(serviceId)).thenReturn(Optional.of(service));
        when(endpointRepository.findByApiServiceId(serviceId)).thenReturn(List.of(endpoint1, endpoint2));

        apiServiceImpl.deleteService(serviceId);

        // Verify deleteEndpoint was called for every associated endpoint
        verify(endpointService, times(1)).deleteEndpoint(100L);
        verify(endpointService, times(1)).deleteEndpoint(200L);

        // Verify service itself was deleted
        verify(apiServiceRepository, times(1)).delete(service);
    }

    @Test
    void testDeleteEndpointCascadesToAlertsAndMetricsAndDeletesEndpoint() {
        Long endpointId = 100L;
        Endpoint endpoint = new Endpoint();
        endpoint.setId(endpointId);

        when(endpointRepository.findById(endpointId)).thenReturn(Optional.of(endpoint));

        endpointServiceImpl.deleteEndpoint(endpointId);

        // Verify alerts and metrics are deleted for endpoint
        verify(alertsRepository, times(1)).deleteByEndpoint(endpoint);
        verify(metricRepository, times(1)).deleteByEndpoint(endpoint);

        // Verify endpoint itself was deleted
        verify(endpointRepository, times(1)).delete(endpoint);
    }
}
