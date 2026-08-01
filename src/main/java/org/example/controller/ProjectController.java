package org.example.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.ProjectRequestDTO;
import org.example.dto.ProjectResponseDTO;
import org.example.service.ProjectService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Projects")
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<ProjectResponseDTO>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> getProjectById(@PathVariable Long id) {
        return projectService.getProjectById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<ProjectResponseDTO> getProjectByName(
            @PathVariable String name) {
        return projectService.getProjectByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<ProjectResponseDTO>> getCreatedAt(@PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(projectService.getCreatedAt(date));
    }

    @GetMapping("/user/{ownerId}")
    public ResponseEntity<List<ProjectResponseDTO>> getProjectsByOwnerId(@PathVariable Long ownerId) {
        return ResponseEntity.ok(projectService.getProjectsByOwner_Id(ownerId));
    }

    @PostMapping
    public ResponseEntity<ProjectResponseDTO> addProject(
            @Valid @RequestBody ProjectRequestDTO requestDTO) {
        ProjectResponseDTO project = projectService.addProject(requestDTO);
        return new ResponseEntity<>(project, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> updateProject(@PathVariable Long id,
            @Valid @RequestBody ProjectRequestDTO requestDTO) {
        boolean updated = projectService.updateProject(id, requestDTO);
        if (updated) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}