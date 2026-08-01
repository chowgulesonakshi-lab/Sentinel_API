package org.example.serviceImplDb;

import org.example.entity.AppUser;
import org.example.repository.AppUserRepository;
import org.example.service.UserService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final AppUserRepository appUserRepository;

    public UserServiceImpl(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public AppUser saveUser(AppUser user) {
        return appUserRepository.save(user);
    }

    @Override
    public Optional<AppUser> findByName(String username) {
        return appUserRepository.findByName(username);
    }

    @Override
    public boolean existsByUsername(String username) {
        return appUserRepository.existsByName(username);
    }
}
