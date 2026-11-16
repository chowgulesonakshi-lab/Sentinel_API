package org.example.service;

import org.example.dto.DependencyImpactResponseDTO;
import org.example.dto.ServiceDependencyGraphResponseDTO;
import org.example.dto.ServiceDependencyRequestDTO;
import org.example.dto.ServiceDependencyResponseDTO;

import java.util.List;

public interface ServiceDependencyService {
    ServiceDependencyResponseDTO create(ServiceDependencyRequestDTO request);
    ServiceDependencyResponseDTO update(Long id, ServiceDependencyRequestDTO request);
    void delete(Long id);
    List<ServiceDependencyResponseDTO> listByProject(Long projectId);
    List<ServiceDependencyResponseDTO> listForService(Long serviceId);
    ServiceDependencyGraphResponseDTO graph(Long projectId);
    DependencyImpactResponseDTO impact(Long projectId, Long serviceId);
}
