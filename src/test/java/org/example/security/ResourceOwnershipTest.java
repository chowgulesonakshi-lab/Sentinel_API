package org.example.security;

import org.example.entity.ApiService;
import org.example.entity.AppUser;
import org.example.entity.Endpoint;
import org.example.entity.Project;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;

class ResourceOwnershipTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void allowsOwnerAndRejectsDifferentUser() {
        AppUser owner = new AppUser();
        owner.setEmail("owner@example.com");
        Project project = new Project();
        project.setOwner(owner);

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("owner@example.com", "password", "ROLE_VIEWER"));
        assertTrue(ResourceOwnership.isCurrentUser(project));

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("other@example.com", "password", "ROLE_VIEWER"));
        assertFalse(ResourceOwnership.isCurrentUser(project));
        assertThrows(AccessDeniedException.class, () -> ResourceOwnership.requireOwner(project));
    }

    @Test
    void ownershipFlowsFromProjectToServiceAndEndpoint() {
        AppUser owner = new AppUser();
        owner.setEmail("owner@example.com");
        Project project = new Project();
        project.setOwner(owner);

        ApiService service = new ApiService();
        service.setProject(project);
        Endpoint endpoint = new Endpoint();
        endpoint.setApiService(service);

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("owner@example.com", "password", "ROLE_VIEWER"));
        assertTrue(ResourceOwnership.isCurrentUser(service));
        assertTrue(ResourceOwnership.isCurrentUser(endpoint));

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("intruder@example.com", "password", "ROLE_VIEWER"));
        assertFalse(ResourceOwnership.isCurrentUser(service));
        assertFalse(ResourceOwnership.isCurrentUser(endpoint));
    }
}
