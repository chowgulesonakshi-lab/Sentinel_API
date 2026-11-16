package org.example.serviceImplDb;

import lombok.RequiredArgsConstructor;
import org.example.dto.ProjectRequestDTO;
import org.example.dto.ProjectResponseDTO;
import org.example.entity.ApiService;
import org.example.entity.AppUser;
import org.example.entity.Project;
import org.example.repository.ApiServiceRepository;
import org.example.repository.AppUserRepository;
import org.example.repository.ProjectRepository;
import org.example.service.ApiService_Service;
import org.example.service.ProjectService;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Profile("db")
@Transactional
public class ProjectServiceImplDb implements ProjectService {
    private final ProjectRepository projectRepository;
    private final AppUserRepository appUserRepository;
    private final ApiServiceRepository apiServiceRepository;
    private final ApiService_Service apiServiceService;

    private String currentEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null || authentication.getName().equals("anonymousUser")) {
            throw new org.springframework.security.access.AccessDeniedException("Authentication required");
        }
        return authentication.getName();
    }

    private Project ownedProject(Long id) {
        return projectRepository.findByIdAndOwnerEmail(id, currentEmail())
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Project not found"));
    }

    private ProjectResponseDTO mapToDTO(Project project) {
        ProjectResponseDTO dto = new ProjectResponseDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setOwnerId(project.getOwner().getId());
        dto.setOwnerName(project.getOwner().getName());
        dto.setCreatedAt(project.getCreatedAt());
        return dto;
    }

    @Override
    public Optional<ProjectResponseDTO> getProjectById(Long id) {
        return projectRepository.findByIdAndOwnerEmail(id, currentEmail()).map(this::mapToDTO);
    }

    @Override
    public List<ProjectResponseDTO> getCreatedAt(LocalDate date) {
        return projectRepository.findByCreatedAtAndOwnerEmail(date, currentEmail()).stream()
                .map(this::mapToDTO).toList();
    }

    @Override
    public ProjectResponseDTO addProject(ProjectRequestDTO requestDTO) {
        AppUser owner = appUserRepository.findByEmail(currentEmail())
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("User not found"));
        Project project = new Project();
        project.setName(requestDTO.getName());
        project.setDescription(requestDTO.getDescription());
        project.setOwner(owner);
        return mapToDTO(projectRepository.save(project));
    }

    @Override
    public boolean updateProject(Long id, ProjectRequestDTO requestDTO) {
        Project project = ownedProject(id);
        project.setName(requestDTO.getName());
        project.setDescription(requestDTO.getDescription());
        projectRepository.save(project);
        return true;
    }

    @Override
    public void deleteProject(Long id) {
        Project project = ownedProject(id);
        List<ApiService> services = apiServiceRepository.findByProjectId(id);
        for (ApiService service : services) {
            apiServiceService.deleteService(service.getId());
        }
        projectRepository.delete(project);
    }

    @Override
    public List<ProjectResponseDTO> getAllProjects() {
        return projectRepository.findByOwnerEmail(currentEmail()).stream().map(this::mapToDTO).toList();
    }

    @Override
    public Optional<ProjectResponseDTO> getProjectByName(String name) {
        return projectRepository.findByNameAndOwnerEmail(name, currentEmail()).map(this::mapToDTO);
    }

    @Override
    public List<ProjectResponseDTO> getProjectsByOwner_Id(Long ownerId) {
        // Ignore caller-supplied owner ID: users may only enumerate their own projects.
        AppUser current = appUserRepository.findByEmail(currentEmail())
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("User not found"));
        return projectRepository.findByOwnerId(current.getId()).stream().map(this::mapToDTO).toList();
    }
}
