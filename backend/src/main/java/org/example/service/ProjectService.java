package org.example.service;

import org.example.dto.ProjectRequestDTO;
import org.example.dto.ProjectResponseDTO;
import org.example.entity.Project;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProjectService {
    Optional<ProjectResponseDTO> getProjectById(Long id);
    List<ProjectResponseDTO> getCreatedAt(LocalDate date);
    ProjectResponseDTO addProject(ProjectRequestDTO projectRequestDTO);
    boolean updateProject(Long id, ProjectRequestDTO projectRequestDTO);
    void deleteProject(Long id);
    List<ProjectResponseDTO> getAllProjects();
    Optional<ProjectResponseDTO> getProjectByName(String name);
    List<ProjectResponseDTO> getProjectsByOwner_Id(Long ownerId);
}
