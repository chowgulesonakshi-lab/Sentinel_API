package org.example.serviceImplDb;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.entity.ApiService;
import org.example.entity.AppUser;
import org.example.entity.Endpoint;
import org.example.entity.Project;
import org.example.repository.ApiServiceRepository;
import org.example.repository.EndpointRepository;
import org.example.repository.ProjectRepository;
import org.example.service.DemoWorkspaceService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class DemoWorkSpaceServiceImpl implements DemoWorkspaceService {
    private final ProjectRepository projectRepository;
    private final ApiServiceRepository apiServiceRepository;
    private final EndpointRepository endpointRepository;

    @Override
    public void createDemoWorkspace(AppUser user) {

        // Create Project
        Project project = new Project();
        project.setName("Demo API Monitoring");
        project.setDescription(
                "Demo workspace created automatically. Feel free to edit or delete."
        );
        project.setOwner(user);

        projectRepository.save(project);

        // ---------------- User Service ----------------

        ApiService userService = new ApiService();
        userService.setName("User Service");
        userService.setDescription("Demo user APIs");
        userService.setProject(project);
        userService.setStatus(ApiService.ServiceStatus.ACTIVE);

        apiServiceRepository.save(userService);

        // Endpoint 1
        Endpoint users = new Endpoint();
        users.setMethod(Endpoint.HttpMethod.GET);
        users.setUrl("https://jsonplaceholder.typicode.com/users");
        users.setTimeoutMillis(3000);
        users.setEnabled(true);
        users.setApiService(userService);

        endpointRepository.save(users);

        // Endpoint 2
        Endpoint posts = new Endpoint();
        posts.setMethod(Endpoint.HttpMethod.GET);
        posts.setUrl("https://jsonplaceholder.typicode.com/posts");
        posts.setTimeoutMillis(3000);
        posts.setEnabled(true);
        posts.setApiService(userService);

        endpointRepository.save(posts);

        // ---------------- GitHub Service ----------------

        ApiService github = new ApiService();
        github.setName("GitHub API");
        github.setDescription("GitHub public APIs");
        github.setProject(project);
        github.setStatus(ApiService.ServiceStatus.ACTIVE);

        apiServiceRepository.save(github);

        Endpoint githubEndpoint = new Endpoint();
        githubEndpoint.setMethod(Endpoint.HttpMethod.GET);
        githubEndpoint.setUrl("https://api.github.com");
        githubEndpoint.setTimeoutMillis(3000);
        githubEndpoint.setEnabled(true);
        githubEndpoint.setApiService(github);

        endpointRepository.save(githubEndpoint);

        // ---------------- Testing APIs ----------------

        ApiService testing = new ApiService();
        testing.setName("Testing APIs");
        testing.setDescription("Used for alerts and slow APIs");
        testing.setProject(project);
        testing.setStatus(ApiService.ServiceStatus.ACTIVE);

        apiServiceRepository.save(testing);

        Endpoint delay = new Endpoint();
        delay.setMethod(Endpoint.HttpMethod.GET);
        delay.setUrl("https://httpbin.org/delay/2");
        delay.setTimeoutMillis(3000);
        delay.setEnabled(true);
        delay.setApiService(testing);

        endpointRepository.save(delay);

        Endpoint error = new Endpoint();
        error.setMethod(Endpoint.HttpMethod.GET);
        error.setUrl("https://httpbin.org/status/500");
        error.setTimeoutMillis(3000);
        error.setEnabled(true);
        error.setApiService(testing);

        endpointRepository.save(error);
    }
}
