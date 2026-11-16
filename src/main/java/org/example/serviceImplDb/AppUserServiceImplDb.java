package org.example.serviceImplDb;

import org.example.entity.AppUser;
import org.example.repository.AppUserRepository;
import org.example.service.AppUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

@Service
@Profile("db")
public class AppUserServiceImplDb implements AppUserService {
    private final AppUserRepository appUserRepository;

    public AppUserServiceImplDb(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    private AppUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
            throw new AccessDeniedException("Authentication required");
        }
        return appUserRepository.findByEmail(authentication.getName().trim().toLowerCase())
                .orElseThrow(() -> new AccessDeniedException("User not found"));
    }

    @Override
    public Optional<AppUser> getUserById(Long id) {
        AppUser current = currentUser();
        return current.getId().equals(id) ? Optional.of(current) : Optional.empty();
    }

    @Override
    public List<AppUser> getUserByRole(AppUser.Role role) {
        AppUser current = currentUser();
        return current.getRole() == role ? List.of(current) : List.of();
    }

    @Override
    public AppUser addUser(AppUser appUser) {
        throw new AccessDeniedException("Use the public signup endpoint to register an account");
    }

    @Override
    public boolean updateUser(Long id, AppUser appUser) {
        AppUser user = currentUser();
        if (!user.getId().equals(id)) {
            throw new AccessDeniedException("Cannot modify another user's account");
        }
        user.setName(appUser.getName());
        if (appUser.getPassword() != null && !appUser.getPassword().isBlank()) {
            user.setPassword(passwordEncoder().encode(appUser.getPassword()));
        }
        appUserRepository.save(user);
        return true;
    }

    @Override
    public boolean deleteUser(Long id) {
        throw new AccessDeniedException("Account deletion is not available through this endpoint");
    }

    @Override
    public List<AppUser> getAllUsers() {
        return List.of(currentUser());
    }

    @Override
    public Optional<AppUser> getUserByName(String name) {
        AppUser current = currentUser();
        return current.getName() != null && current.getName().equals(name) ? Optional.of(current) : Optional.empty();
    }
}
