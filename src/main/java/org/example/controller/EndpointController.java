package org.example.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.EndpointRequestDTO;
import org.example.dto.EndpointResponseDTO;
import org.example.entity.Endpoint;
import org.example.service.EndpointService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Endpoints")
@RestController
@RequestMapping("/endpoints")
@RequiredArgsConstructor
public class EndpointController {
    private final EndpointService endpointService;

    @GetMapping
    public ResponseEntity<List<EndpointResponseDTO>> getAllEndpoints() {
        return ResponseEntity.ok(endpointService.getAllEndpoints());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EndpointResponseDTO> getEndpointById(@PathVariable Long id) {
        return endpointService.getEndpointById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/method/{method}")
    public ResponseEntity<List<EndpointResponseDTO>> getEndpointsByMethod(@PathVariable Endpoint.HttpMethod method) {
        return ResponseEntity.ok(endpointService.getEndpointsByMethod(method));
    }

    @GetMapping("/greater-timeout/{timeout}")
    public ResponseEntity<List<EndpointResponseDTO>> findByTimeoutGreaterThan(@PathVariable int timeout) {
        return ResponseEntity.ok(endpointService.getEndpointsByTimeoutGreaterThan(timeout));
    }

    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<EndpointResponseDTO>> getEndpointsByServiceId(@PathVariable Long serviceId) {
        return ResponseEntity.ok(endpointService.getEndpointsByApiService_Id(serviceId));
    }

    @PostMapping
    public ResponseEntity<EndpointResponseDTO> addEndpoint(
            @Valid @RequestBody EndpointRequestDTO requestDTO) {
        EndpointResponseDTO endpoint = endpointService.addEndpoint(requestDTO);
        return new ResponseEntity<>(endpoint, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EndpointResponseDTO> updateEndpoint(
            @PathVariable Long id,
            @Valid @RequestBody EndpointRequestDTO requestDTO) {
        EndpointResponseDTO endpoint = endpointService.updateEndpoint(id, requestDTO);
        return ResponseEntity.ok(endpoint);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEndpoint(@PathVariable Long id) {
        endpointService.deleteEndpoint(id);
        return ResponseEntity.noContent().build();
    }
}