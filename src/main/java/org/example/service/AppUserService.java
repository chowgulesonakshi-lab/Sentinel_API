package org.example.service;

import org.example.entity.AppUser;

import java.util.List;
import java.util.Optional;

public interface AppUserService {
    Optional<AppUser> getUserById(Long id);
    List<AppUser> getUserByRole (AppUser.Role role);
    AppUser addUser(AppUser appUser);
    boolean updateUser(Long id, AppUser appUser);
    boolean deleteUser(Long id);
    List<AppUser> getAllUsers();
    Optional<AppUser> getUserByName(String name);
}
