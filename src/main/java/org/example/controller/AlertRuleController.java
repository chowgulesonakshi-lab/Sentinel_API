package org.example.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.AlertRuleRequestDTO;
import org.example.dto.AlertRuleResponseDTO;
import org.example.service.AlertRuleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Alert Rules")
@RestController
@RequestMapping("/alert-rules")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AlertRuleController {

    private final AlertRuleService alertRuleService;

    @GetMapping
    public ResponseEntity<List<AlertRuleResponseDTO>> getAllRules() {
        return ResponseEntity.ok(alertRuleService.getAllRules());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertRuleResponseDTO> getRuleById(@PathVariable Long id) {
        return alertRuleService.getRuleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/endpoint/{endpointId}")
    public ResponseEntity<List<AlertRuleResponseDTO>> getRulesByEndpoint(@PathVariable Long endpointId) {
        return ResponseEntity.ok(alertRuleService.getRulesByEndpoint(endpointId));
    }

    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<AlertRuleResponseDTO>> getRulesByService(@PathVariable Long serviceId) {
        return ResponseEntity.ok(alertRuleService.getRulesByService(serviceId));
    }

    @PostMapping
    public ResponseEntity<AlertRuleResponseDTO> createRule(@Valid @RequestBody AlertRuleRequestDTO requestDTO) {
        return new ResponseEntity<>(alertRuleService.createRule(requestDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlertRuleResponseDTO> updateRule(
            @PathVariable Long id,
            @Valid @RequestBody AlertRuleRequestDTO requestDTO) {
        return ResponseEntity.ok(alertRuleService.updateRule(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        alertRuleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<AlertRuleResponseDTO> toggleRule(@PathVariable Long id) {
        return ResponseEntity.ok(alertRuleService.toggleRule(id));
    }
}
