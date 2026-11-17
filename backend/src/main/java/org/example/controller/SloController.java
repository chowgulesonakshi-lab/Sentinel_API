package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.SloReportDTO;
import org.example.dto.SloTargetRequestDTO;
import org.example.service.SloService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/slos")
@RequiredArgsConstructor
public class SloController {
    private final SloService sloService;

    @PutMapping("/endpoints/{endpointId}/target")
    public ResponseEntity<SloReportDTO> updateTarget(
            @PathVariable Long endpointId,
            @Valid @RequestBody SloTargetRequestDTO request) {
        return ResponseEntity.ok(sloService.updateTarget(endpointId, request));
    }

    @GetMapping("/endpoints/{endpointId}/report")
    public ResponseEntity<SloReportDTO> getReport(@PathVariable Long endpointId) {
        return ResponseEntity.ok(sloService.getReport(endpointId));
    }
}
