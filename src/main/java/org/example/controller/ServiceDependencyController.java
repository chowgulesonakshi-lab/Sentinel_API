package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.DependencyImpactResponseDTO;
import org.example.dto.ServiceDependencyGraphResponseDTO;
import org.example.dto.ServiceDependencyRequestDTO;
import org.example.dto.ServiceDependencyResponseDTO;
import org.example.service.ServiceDependencyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/service-dependencies")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ServiceDependencyController {
    private final ServiceDependencyService dependencyService;

    @PostMapping
    public ResponseEntity<ServiceDependencyResponseDTO> create(@Valid @RequestBody ServiceDependencyRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dependencyService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceDependencyResponseDTO> update(@PathVariable Long id, @Valid @RequestBody ServiceDependencyRequestDTO request) {
        return ResponseEntity.ok(dependencyService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        dependencyService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ServiceDependencyResponseDTO>> listByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(dependencyService.listByProject(projectId));
    }

    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<ServiceDependencyResponseDTO>> listForService(@PathVariable Long serviceId) {
        return ResponseEntity.ok(dependencyService.listForService(serviceId));
    }

    @GetMapping("/project/{projectId}/graph")
    public ResponseEntity<ServiceDependencyGraphResponseDTO> graph(@PathVariable Long projectId) {
        return ResponseEntity.ok(dependencyService.graph(projectId));
    }

    @GetMapping("/project/{projectId}/impact/{serviceId}")
    public ResponseEntity<DependencyImpactResponseDTO> impact(@PathVariable Long projectId, @PathVariable Long serviceId) {
        return ResponseEntity.ok(dependencyService.impact(projectId, serviceId));
    }
}
