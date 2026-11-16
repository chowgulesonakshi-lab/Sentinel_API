package org.example.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.ApiServiceRequestDTO;
import org.example.dto.ApiServiceResponseDTO;
import org.example.entity.ApiService;
import org.example.service.ApiService_Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "API Services")
@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
public class ApiServiceController {
    private final ApiService_Service apiService;

    @GetMapping
    public ResponseEntity<List<ApiServiceResponseDTO>> getAllServices() {
        return ResponseEntity.ok(apiService.getAllServices());
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<ApiServiceResponseDTO> getServiceById(@PathVariable Long id) {
        return apiService.getServiceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<List<ApiServiceResponseDTO>> getServiceByName(@PathVariable String name) {
        return ResponseEntity.ok(apiService.getServiceByName(name));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ApiServiceResponseDTO>> getServiceByStatus(@PathVariable ApiService.ServiceStatus status) {
        return ResponseEntity.ok(apiService.getServiceByStatus(status));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ApiServiceResponseDTO>> getServicesByProject(
            @PathVariable Long projectId) {

        return ResponseEntity.ok(apiService.getServicesByProject_Id(projectId));
    }

    @PostMapping
    public ResponseEntity<ApiServiceResponseDTO> addService(
            @Valid @RequestBody ApiServiceRequestDTO requestDTO) {

        ApiServiceResponseDTO service = apiService.addService(requestDTO);

        return new ResponseEntity<>(service, HttpStatus.CREATED);
    }

    @PutMapping("/id/{id}")
    public ResponseEntity<ApiServiceResponseDTO> updateService(@PathVariable Long id,
            @Valid @RequestBody ApiServiceRequestDTO requestDTO) {
        ApiServiceResponseDTO updatedService = apiService.updateService(id, requestDTO);
        return ResponseEntity.ok(updatedService);
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        apiService.deleteService(id);
        return ResponseEntity.noContent().build();
    }
}