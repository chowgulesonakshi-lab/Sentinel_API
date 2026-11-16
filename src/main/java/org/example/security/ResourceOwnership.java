package org.example.security;

import org.example.entity.ApiService;
import org.example.entity.Endpoint;
import org.example.entity.Project;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Locale;

public final class ResourceOwnership {
    private ResourceOwnership() {}

    public static boolean isCurrentUser(Project project) {
        return project != null && project.getOwner() != null
                && project.getOwner().getEmail() != null
                && project.getOwner().getEmail().trim().equalsIgnoreCase(currentEmail());
    }

    public static boolean isCurrentUser(ApiService service) {
        return service != null && isCurrentUser(service.getProject());
    }

    public static boolean isCurrentUser(Endpoint endpoint) {
        return endpoint != null && endpoint.getApiService() != null
                && isCurrentUser(endpoint.getApiService());
    }

    public static void requireOwner(Project project) {
        if (!isCurrentUser(project)) throw new AccessDeniedException("Resource not found");
    }

    public static void requireOwner(ApiService service) {
        if (!isCurrentUser(service)) throw new AccessDeniedException("Resource not found");
    }

    public static void requireOwner(Endpoint endpoint) {
        if (!isCurrentUser(endpoint)) throw new AccessDeniedException("Resource not found");
    }

    private static String currentEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            throw new AccessDeniedException("Authentication required");
        }
        return authentication.getName().trim().toLowerCase(Locale.ROOT);
    }
}
