package org.example.serviceImplDb;

import lombok.RequiredArgsConstructor;
import org.example.dto.AlertRuleRequestDTO;
import org.example.dto.AlertRuleResponseDTO;
import org.example.entity.AlertRule;
import org.example.entity.ApiService;
import org.example.entity.Endpoint;
import org.example.repository.AlertRuleRepository;
import org.example.repository.ApiServiceRepository;
import org.example.repository.EndpointRepository;
import org.example.service.AlertRuleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AlertRuleServiceImpl implements AlertRuleService {

    private final AlertRuleRepository alertRuleRepository;
    private final EndpointRepository endpointRepository;
    private final ApiServiceRepository apiServiceRepository;

    private AlertRuleResponseDTO mapToDTO(AlertRule rule) {
        AlertRuleResponseDTO dto = new AlertRuleResponseDTO();
        dto.setId(rule.getId());
        dto.setName(rule.getName());
        dto.setConditionType(rule.getConditionType());
        dto.setConditionParameters(rule.getConditionParameters());
        dto.setSeverity(rule.getSeverity());
        dto.setEnabled(rule.isEnabled());
        dto.setEvaluationWindowMinutes(rule.getEvaluationWindowMinutes());
        dto.setCreatedAt(rule.getCreatedAt());
        dto.setUpdatedAt(rule.getUpdatedAt());

        if (rule.getEndpoint() != null) {
            dto.setEndpointId(rule.getEndpoint().getId());
            dto.setEndpointUrl(rule.getEndpoint().getUrl());
            dto.setEndpointMethod(rule.getEndpoint().getMethod() != null ? rule.getEndpoint().getMethod().name() : null);
        }

        if (rule.getApiService() != null) {
            dto.setApiServiceId(rule.getApiService().getId());
            dto.setApiServiceName(rule.getApiService().getName());
        }

        return dto;
    }

    @Override
    public AlertRuleResponseDTO createRule(AlertRuleRequestDTO requestDTO) {
        AlertRule rule = new AlertRule();
        rule.setName(requestDTO.getName());
        rule.setConditionType(requestDTO.getConditionType());
        rule.setConditionParameters(requestDTO.getConditionParameters());
        rule.setSeverity(requestDTO.getSeverity());
        rule.setEnabled(requestDTO.isEnabled());
        rule.setEvaluationWindowMinutes(requestDTO.getEvaluationWindowMinutes() != null ? requestDTO.getEvaluationWindowMinutes() : 15);

        if (requestDTO.getEndpointId() != null) {
            Endpoint endpoint = endpointRepository.findById(requestDTO.getEndpointId())
                    .orElseThrow(() -> new RuntimeException("Endpoint not found with ID: " + requestDTO.getEndpointId()));
            rule.setEndpoint(endpoint);
            rule.setApiService(endpoint.getApiService());
        } else if (requestDTO.getApiServiceId() != null) {
            ApiService service = apiServiceRepository.findById(requestDTO.getApiServiceId())
                    .orElseThrow(() -> new RuntimeException("API Service not found with ID: " + requestDTO.getApiServiceId()));
            rule.setApiService(service);
        }

        AlertRule saved = alertRuleRepository.save(rule);
        return mapToDTO(saved);
    }

    @Override
    public AlertRuleResponseDTO updateRule(Long id, AlertRuleRequestDTO requestDTO) {
        AlertRule rule = alertRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert Rule not found with ID: " + id));

        rule.setName(requestDTO.getName());
        rule.setConditionType(requestDTO.getConditionType());
        rule.setConditionParameters(requestDTO.getConditionParameters());
        rule.setSeverity(requestDTO.getSeverity());
        rule.setEnabled(requestDTO.isEnabled());
        if (requestDTO.getEvaluationWindowMinutes() != null) {
            rule.setEvaluationWindowMinutes(requestDTO.getEvaluationWindowMinutes());
        }

        if (requestDTO.getEndpointId() != null) {
            Endpoint endpoint = endpointRepository.findById(requestDTO.getEndpointId())
                    .orElseThrow(() -> new RuntimeException("Endpoint not found with ID: " + requestDTO.getEndpointId()));
            rule.setEndpoint(endpoint);
            rule.setApiService(endpoint.getApiService());
        } else if (requestDTO.getApiServiceId() != null) {
            ApiService service = apiServiceRepository.findById(requestDTO.getApiServiceId())
                    .orElseThrow(() -> new RuntimeException("API Service not found with ID: " + requestDTO.getApiServiceId()));
            rule.setApiService(service);
            rule.setEndpoint(null);
        } else {
            rule.setEndpoint(null);
            rule.setApiService(null);
        }

        AlertRule saved = alertRuleRepository.save(rule);
        return mapToDTO(saved);
    }

    @Override
    public void deleteRule(Long id) {
        AlertRule rule = alertRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert Rule not found with ID: " + id));
        alertRuleRepository.delete(rule);
    }

    @Override
    public List<AlertRuleResponseDTO> getAllRules() {
        return alertRuleRepository.findAll().stream().map(this::mapToDTO).toList();
    }

    @Override
    public Optional<AlertRuleResponseDTO> getRuleById(Long id) {
        return alertRuleRepository.findById(id).map(this::mapToDTO);
    }

    @Override
    public List<AlertRuleResponseDTO> getRulesByEndpoint(Long endpointId) {
        return alertRuleRepository.findByEndpointId(endpointId).stream().map(this::mapToDTO).toList();
    }

    @Override
    public List<AlertRuleResponseDTO> getRulesByService(Long serviceId) {
        return alertRuleRepository.findByApiServiceId(serviceId).stream().map(this::mapToDTO).toList();
    }

    @Override
    public AlertRuleResponseDTO toggleRule(Long id) {
        AlertRule rule = alertRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert Rule not found with ID: " + id));
        rule.setEnabled(!rule.isEnabled());
        AlertRule saved = alertRuleRepository.save(rule);
        return mapToDTO(saved);
    }
}
