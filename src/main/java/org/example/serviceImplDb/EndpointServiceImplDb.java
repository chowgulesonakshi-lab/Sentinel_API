package org.example.serviceImplDb;

import lombok.RequiredArgsConstructor;
import org.example.dto.EndpointRequestDTO;
import org.example.dto.EndpointResponseDTO;
import org.example.entity.ApiService;
import org.example.entity.Endpoint;
import org.example.repository.AlertsRepository;
import org.example.repository.ApiServiceRepository;
import org.example.repository.EndpointRepository;
import org.example.repository.MetricRepository;
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
public class EndpointServiceImplDb implements EndpointService {
    private final EndpointRepository endpointRepository;
    private final ApiServiceRepository apiServiceRepository;
    private final AlertsRepository alertsRepository;
    private final MetricRepository metricRepository;

    private EndpointResponseDTO mapToDTO(Endpoint endpoint) {
        EndpointResponseDTO dto = new EndpointResponseDTO();

        dto.setId(endpoint.getId());
        dto.setMethod(endpoint.getMethod());
        dto.setUrl(endpoint.getUrl());
        dto.setTimeoutMillis(endpoint.getTimeoutMillis());
        dto.setEnabled(endpoint.isEnabled());
        dto.setIntervalSeconds(endpoint.getIntervalSeconds() != null ? endpoint.getIntervalSeconds() : 60);
        dto.setExpectedStatusCodes(endpoint.getExpectedStatusCodes() != null ? endpoint.getExpectedStatusCodes() : "200,201,202,204");
        dto.setSlowResponseThresholdMillis(endpoint.getSlowResponseThresholdMillis() != null ? endpoint.getSlowResponseThresholdMillis() : 1000);
        dto.setConsecutiveFailuresThreshold(endpoint.getConsecutiveFailuresThreshold() != null ? endpoint.getConsecutiveFailuresThreshold() : 1);
        dto.setConsecutiveFailuresCount(endpoint.getConsecutiveFailuresCount() != null ? endpoint.getConsecutiveFailuresCount() : 0);
        dto.setAnomalyDetectionEnabled(!Boolean.FALSE.equals(endpoint.getAnomalyDetectionEnabled()));
        dto.setAnomalySensitivityMultiplier(endpoint.getAnomalySensitivityMultiplier() != null ? endpoint.getAnomalySensitivityMultiplier() : 3.0);
        dto.setAnomalyObservationWindow(endpoint.getAnomalyObservationWindow() != null ? endpoint.getAnomalyObservationWindow() : 20);
        dto.setAnomalyMinimumObservations(endpoint.getAnomalyMinimumObservations() != null ? endpoint.getAnomalyMinimumObservations() : 5);
        dto.setHeaders(endpoint.getHeaders());
        dto.setRequestBody(endpoint.getRequestBody());
        dto.setLastMonitoredAt(endpoint.getLastMonitoredAt());

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
        endpoint.setIntervalSeconds(requestDTO.getIntervalSeconds() != null ? requestDTO.getIntervalSeconds() : 60);
        endpoint.setExpectedStatusCodes(requestDTO.getExpectedStatusCodes() != null && !requestDTO.getExpectedStatusCodes().isBlank()
                ? requestDTO.getExpectedStatusCodes()
                : "200,201,202,204");
        endpoint.setSlowResponseThresholdMillis(requestDTO.getSlowResponseThresholdMillis() != null ? requestDTO.getSlowResponseThresholdMillis() : 1000);
        endpoint.setConsecutiveFailuresThreshold(requestDTO.getConsecutiveFailuresThreshold() != null ? requestDTO.getConsecutiveFailuresThreshold() : 1);
        endpoint.setHeaders(requestDTO.getHeaders());
        endpoint.setRequestBody(requestDTO.getRequestBody());
        endpoint.setConsecutiveFailuresCount(0);
        endpoint.setAnomalyDetectionEnabled(requestDTO.isAnomalyDetectionEnabled());
        endpoint.setAnomalySensitivityMultiplier(requestDTO.getAnomalySensitivityMultiplier());
        endpoint.setAnomalyObservationWindow(requestDTO.getAnomalyObservationWindow());
        endpoint.setAnomalyMinimumObservations(requestDTO.getAnomalyMinimumObservations());
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
        if (requestDTO.getIntervalSeconds() != null) {
            endpoint.setIntervalSeconds(requestDTO.getIntervalSeconds());
        }
        if (requestDTO.getExpectedStatusCodes() != null) {
            endpoint.setExpectedStatusCodes(requestDTO.getExpectedStatusCodes());
        }
        if (requestDTO.getSlowResponseThresholdMillis() != null) {
            endpoint.setSlowResponseThresholdMillis(requestDTO.getSlowResponseThresholdMillis());
        }
        if (requestDTO.getConsecutiveFailuresThreshold() != null) {
            endpoint.setConsecutiveFailuresThreshold(requestDTO.getConsecutiveFailuresThreshold());
        }
        endpoint.setAnomalyDetectionEnabled(requestDTO.isAnomalyDetectionEnabled());
        if (requestDTO.getAnomalySensitivityMultiplier() != null) {
            endpoint.setAnomalySensitivityMultiplier(requestDTO.getAnomalySensitivityMultiplier());
        }
        if (requestDTO.getAnomalyObservationWindow() != null) {
            endpoint.setAnomalyObservationWindow(requestDTO.getAnomalyObservationWindow());
        }
        if (requestDTO.getAnomalyMinimumObservations() != null) {
            endpoint.setAnomalyMinimumObservations(requestDTO.getAnomalyMinimumObservations());
        }
        endpoint.setHeaders(requestDTO.getHeaders());
        endpoint.setRequestBody(requestDTO.getRequestBody());
        endpoint.setApiService(service);

        Endpoint updatedEndpoint = endpointRepository.save(endpoint);
        return mapToDTO(updatedEndpoint);
    }

    @Override
    public void deleteEndpoint(Long id) {
        Endpoint endpoint = endpointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Endpoint not found"));

        alertsRepository.deleteByEndpoint(endpoint);
        metricRepository.deleteByEndpoint(endpoint);
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
