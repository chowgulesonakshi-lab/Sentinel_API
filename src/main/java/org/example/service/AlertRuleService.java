package org.example.service;

import org.example.dto.AlertRuleRequestDTO;
import org.example.dto.AlertRuleResponseDTO;

import java.util.List;
import java.util.Optional;

public interface AlertRuleService {
    AlertRuleResponseDTO createRule(AlertRuleRequestDTO requestDTO);
    AlertRuleResponseDTO updateRule(Long id, AlertRuleRequestDTO requestDTO);
    void deleteRule(Long id);
    List<AlertRuleResponseDTO> getAllRules();
    Optional<AlertRuleResponseDTO> getRuleById(Long id);
    List<AlertRuleResponseDTO> getRulesByEndpoint(Long endpointId);
    List<AlertRuleResponseDTO> getRulesByService(Long serviceId);
    AlertRuleResponseDTO toggleRule(Long id);
}
