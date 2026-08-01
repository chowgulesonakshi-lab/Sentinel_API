package org.example.service;

import org.example.dto.EndpointRequestDTO;
import org.example.dto.EndpointResponseDTO;
import org.example.entity.Endpoint;

import java.util.List;
import java.util.Optional;

public interface EndpointService {
    EndpointResponseDTO addEndpoint (EndpointRequestDTO endpointRequestDTO);
    List<EndpointResponseDTO> getAllEndpoints();
    Optional<EndpointResponseDTO> getEndpointById(Long id);
    EndpointResponseDTO updateEndpoint(Long id, EndpointRequestDTO endpointRequestDTO);
    void deleteEndpoint(Long id);
    List<EndpointResponseDTO> getEndpointsByMethod(Endpoint.HttpMethod method);
    List<EndpointResponseDTO> getEndpointsByTimeoutGreaterThan(int timeout);
    List<EndpointResponseDTO> getEndpointsByApiService_Id(Long serviceId);
}
