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

    @Override
    public Optional<AppUser> getUserById(Long id) {
        return appUserRepository.findById(id);
    }

    @Override
    public List<AppUser> getUserByRole(AppUser.Role role) {
        return appUserRepository.findByRole(role);
    }

    @Override
    public AppUser addUser(AppUser appUser) {
        if(appUserRepository.existsByName(appUser.getName())){
            throw new RuntimeException("Username already exists");
        }
        return appUserRepository.save(appUser);
    }

    @Override
    public boolean updateUser(Long id, AppUser appUser) {
        Optional<AppUser> existingUser = appUserRepository.findById(id);
        if (existingUser.isEmpty()) {
            return false;
        }
        AppUser user = existingUser.get();
        user.setName(appUser.getName());
        user.setEmail(appUser.getEmail());
        user.setRole(appUser.getRole());
        if (appUser.getPassword() != null && !appUser.getPassword().isBlank()) {
            user.setPassword(passwordEncoder().encode(appUser.getPassword()));
        }
        appUserRepository.save(user);
        return true;
    }

    @Override
    public boolean deleteUser(Long id) {
        if (appUserRepository.existsById(id)) {
            appUserRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public List<AppUser> getAllUsers() {
        return appUserRepository.findAll();
    }

    @Override
    public Optional<AppUser> getUserByName(String name) {
        return appUserRepository.findByName(name);
    }
}
