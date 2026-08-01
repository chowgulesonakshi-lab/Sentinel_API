package org.example.repository;

import java.util.List;
import java.util.Optional;

import org.example.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    List<AppUser> findByRole(AppUser.Role role);
    Optional<AppUser> findByName(String name);
    boolean existsByName(String username);
    Optional<AppUser> findByEmail(String email);
}