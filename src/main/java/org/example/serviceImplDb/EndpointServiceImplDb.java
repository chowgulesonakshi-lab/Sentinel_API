package org.example.serviceImplDb;

import lombok.RequiredArgsConstructor;
import org.example.dto.EndpointRequestDTO;
import org.example.dto.EndpointResponseDTO;
import org.example.entity.ApiService;
import org.example.entity.Endpoint;
import org.example.repository.ApiServiceRepository;
import org.example.repository.EndpointRepository;
import org.example.service.EndpointService;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Profile("db")
public class EndpointServiceImplDb implements EndpointService {
    private final EndpointRepository endpointRepository;
    private final ApiServiceRepository apiServiceRepository;

    private EndpointResponseDTO mapToDTO(Endpoint endpoint) {
        EndpointResponseDTO dto = new EndpointResponseDTO();

        dto.setId(endpoint.getId());
        dto.setMethod(endpoint.getMethod());
        dto.setUrl(endpoint.getUrl());
        dto.setTimeoutMillis(endpoint.getTimeoutMillis());
        dto.setEnabled(endpoint.isEnabled());

        dto.setApiServiceId(endpoint.getApiService().getId());
        dto.setApiServiceName(endpoint.getApiService().getName());

        dto.setProjectId(endpoint.getApiService().getProject().getId());
        dto.setProjectName(endpoint.getApiService().getProject().getName());

        return dto;
    }

    @Override
    public EndpointResponseDTO addEndpoint(EndpointRequestDTO requestDTO) {
        ApiService service = apiServiceRepository.findById(requestDTO.getApiServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));

        Endpoint endpoint = new Endpoint();

        endpoint.setMethod(requestDTO.getMethod());
        endpoint.setUrl(requestDTO.getUrl());
        endpoint.setTimeoutMillis(requestDTO.getTimeoutMillis());
        endpoint.setEnabled(requestDTO.isEnabled());
        endpoint.setApiService(service);

        Endpoint savedEndpoint = endpointRepository.save(endpoint);
        return mapToDTO(savedEndpoint);
    }

    @Override
    public List<EndpointResponseDTO> getAllEndpoints() {
        return endpointRepository.findAll().stream().map(this::mapToDTO).toList();
    }

    @Override
    public Optional<EndpointResponseDTO> getEndpointById(Long id) {
        return endpointRepository.findById(id).map(this::mapToDTO);
    }

    @Override
    public EndpointResponseDTO updateEndpoint(Long id, EndpointRequestDTO requestDTO) {
        Endpoint endpoint = endpointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Endpoint not found"));

        ApiService service = apiServiceRepository.findById(requestDTO.getApiServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));


        endpoint.setMethod(requestDTO.getMethod());
        endpoint.setUrl(requestDTO.getUrl());
        endpoint.setTimeoutMillis(requestDTO.getTimeoutMillis());
        endpoint.setEnabled(requestDTO.isEnabled());
        endpoint.setApiService(service);

        Endpoint updatedEndpoint = endpointRepository.save(endpoint);
        return mapToDTO(updatedEndpoint);
    }

    @Override
    public void deleteEndpoint(Long id) {
        Endpoint endpoint = endpointRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Endpoint not found"));
        endpointRepository.delete(endpoint);
    }

    @Override
    public List<EndpointResponseDTO> getEndpointsByMethod(Endpoint.HttpMethod method) {
        return endpointRepository.findByMethod(method)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<EndpointResponseDTO> getEndpointsByTimeoutGreaterThan(int timeout) {
        return endpointRepository.findByTimeoutMillisGreaterThan(timeout)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<EndpointResponseDTO> getEndpointsByApiService_Id(Long serviceId) {
        return endpointRepository.findByApiServiceId(serviceId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }
}
