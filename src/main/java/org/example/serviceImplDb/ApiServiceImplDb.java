package org.example.serviceImplDb;

import lombok.RequiredArgsConstructor;
import org.example.dto.ApiServiceRequestDTO;
import org.example.dto.ApiServiceResponseDTO;
import org.example.entity.ApiService;
import org.example.entity.Project;
import org.example.entity.Endpoint;
import org.example.repository.ApiServiceRepository;
import org.example.repository.EndpointRepository;
import org.example.repository.ProjectRepository;
import org.example.repository.ServiceDependencyRepository;
import org.example.service.ApiService_Service;
import org.example.service.EndpointService;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Profile("db")
@Transactional
public class ApiServiceImplDb implements ApiService_Service {
    private final ApiServiceRepository apiServiceRepository;
    private final ProjectRepository projectRepository;
    private final EndpointRepository endpointRepository;
    private final EndpointService endpointService;
    private final ServiceDependencyRepository serviceDependencyRepository;

    private ApiServiceResponseDTO mapToDTO(ApiService apiService){
        ApiServiceResponseDTO dto = new ApiServiceResponseDTO();

        dto.setId(apiService.getId());
        dto.setName(apiService.getName());
        dto.setDescription(apiService.getDescription());

        dto.setProjectId(apiService.getProject().getId());
        dto.setProjectName(apiService.getProject().getName());
        dto.setStatus(apiService.getStatus());
        return dto;
    }

    @Override
    public ApiServiceResponseDTO addService(ApiServiceRequestDTO requestDTO) {
        Project project = projectRepository.findById(requestDTO.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));
        ApiService service = new ApiService();

        service.setName(requestDTO.getName());
        service.setDescription(requestDTO.getDescription());
        service.setStatus(requestDTO.getStatus());
        service.setProject(project);

        ApiService savedService = apiServiceRepository.save(service);
        return mapToDTO(savedService);
    }

    @Override
    public List<ApiServiceResponseDTO> getAllServices() {
        return apiServiceRepository.findAll().stream()
                .map(this::mapToDTO).toList();
    }

    @Override
    public Optional<ApiServiceResponseDTO> getServiceById(Long id) {
        return apiServiceRepository.findById(id).map(this::mapToDTO);
    }

    @Override
    public List<ApiServiceResponseDTO> getServiceByName(String name) {
        return apiServiceRepository.findByName(name).stream().map(this::mapToDTO).toList();
    }

    @Override
    public ApiServiceResponseDTO updateService(Long id, ApiServiceRequestDTO requestDTO) {
        ApiService service = apiServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        Project project = projectRepository.findById(requestDTO.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        service.setName(requestDTO.getName());
        service.setDescription(requestDTO.getDescription());
        service.setStatus(requestDTO.getStatus());
        service.setProject(project);

        ApiService updatedService = apiServiceRepository.save(service);
        return mapToDTO(updatedService);
    }

    @Override
    @Transactional
    public void deleteService(Long id) {
        ApiService service = apiServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        List<Endpoint> endpoints = endpointRepository.findByApiServiceId(id);
        if (serviceDependencyRepository != null) {
            serviceDependencyRepository.deleteByServiceId(id);
        }
        for (Endpoint endpoint : endpoints) {
            endpointService.deleteEndpoint(endpoint.getId());
        }

        apiServiceRepository.delete(service);
    }

    @Override
    public List<ApiServiceResponseDTO> getServiceByStatus(ApiService.ServiceStatus status) {
        return apiServiceRepository.findByStatus(status)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }


    @Override
    public List<ApiServiceResponseDTO> getServicesByProject_Id(Long projectId) {
        return apiServiceRepository.findByProjectId(projectId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }
}
