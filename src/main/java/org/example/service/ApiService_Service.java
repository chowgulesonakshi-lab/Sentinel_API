package org.example.service;

import org.example.dto.ApiServiceRequestDTO;
import org.example.dto.ApiServiceResponseDTO;
import org.example.entity.ApiService;

import java.util.List;
import java.util.Optional;

public interface ApiService_Service {
    ApiServiceResponseDTO addService(ApiServiceRequestDTO apiServiceRequestDTO);
    List<ApiServiceResponseDTO> getAllServices();
    Optional<ApiServiceResponseDTO> getServiceById(Long id);
    List<ApiServiceResponseDTO> getServiceByName(String name);
    ApiServiceResponseDTO  updateService(Long id, ApiServiceRequestDTO apiServiceRequestDTO);
    void deleteService(Long id);
    List<ApiServiceResponseDTO> getServiceByStatus(ApiService.ServiceStatus status);
    List<ApiServiceResponseDTO> getServicesByProject_Id(Long projectId);
}
