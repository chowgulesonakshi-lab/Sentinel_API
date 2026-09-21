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
import org.springframework.beans.BeanUtils;
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

    private ProjectResponseDTO mapToDTO(Project project){
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
        return projectRepository.findById(id).map(this::mapToDTO);
    }

    @Override
    public List<ProjectResponseDTO> getCreatedAt(LocalDate date) {
        return projectRepository.findByCreatedAt(date).stream().
                map(this::mapToDTO).toList();
    }

    @Override
    public ProjectResponseDTO addProject(ProjectRequestDTO requestDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        AppUser owner = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Project project = new Project();

        project.setName(requestDTO.getName());
        project.setDescription(requestDTO.getDescription());
        project.setOwner(owner);

        Project savedProject = projectRepository.save(project);
        return mapToDTO(savedProject);
    }

    @Override
    public boolean updateProject(Long id, ProjectRequestDTO requestDTO) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        AppUser owner = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        project.setName(requestDTO.getName());
        project.setDescription(requestDTO.getDescription());

        projectRepository.save(project);
        return true;
    }

    @Override
    @Transactional
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project Not Found"));

        List<ApiService> services = apiServiceRepository.findByProjectId(id);
        for (ApiService service : services) {
            apiServiceService.deleteService(service.getId());
        }

        projectRepository.delete(project);
    }

    @Override
    public List<ProjectResponseDTO> getAllProjects() {
        return projectRepository.findAll().stream().map(this::mapToDTO).toList();
    }

    @Override
    public Optional<ProjectResponseDTO> getProjectByName(String name) {
        return projectRepository.findByName(name).map(this::mapToDTO);
    }

    @Override
    public List<ProjectResponseDTO> getProjectsByOwner_Id(Long ownerId) {
        return projectRepository.findByOwnerId(ownerId).stream().map(this::mapToDTO).toList();
    }
}
