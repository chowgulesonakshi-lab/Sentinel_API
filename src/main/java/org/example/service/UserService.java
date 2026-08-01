package org.example.service;

import org.example.entity.AppUser;

import java.util.Optional;

public interface UserService {
    AppUser saveUser(AppUser user);
    Optional<AppUser> findByName(String username);
    boolean existsByUsername(String username);
}
