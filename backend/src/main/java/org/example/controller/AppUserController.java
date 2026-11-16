package org.example.controller;

import org.example.entity.AppUser;
import org.example.service.AppUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "User Management", description = "Operations related to users")
@RestController
public class AppUserController {
    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<AppUser>> getAllUsers(){
        return ResponseEntity.ok(appUserService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<Optional<AppUser>> getUserById(@PathVariable Long id){
        return ResponseEntity.ok(appUserService.getUserById(id));
    }

    @GetMapping("/users/role/{role}")
    public ResponseEntity<List<AppUser>> getUserByRole(@PathVariable AppUser.Role role){
        return ResponseEntity.ok(appUserService.getUserByRole(role));
    }

    @GetMapping("/users/name/{name}")
    public ResponseEntity<Optional<AppUser>> getUserByName(@PathVariable String name){
        return ResponseEntity.ok(appUserService.getUserByName(name));
    }

    @PostMapping("/users")
    public ResponseEntity<AppUser> addUser(@RequestBody AppUser appUser){
        AppUser newAppUser = appUserService.addUser(appUser);
        return new ResponseEntity<>(newAppUser, HttpStatus.CREATED);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<AppUser> updateUser(@PathVariable Long id,
                                              @RequestBody AppUser appUser){
        boolean isUpdated = appUserService.updateUser(id, appUser);
        if(isUpdated){
            return ResponseEntity.ok(appUserService.getUserById(id).get());
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        boolean isDeleted = appUserService.deleteUser(id);
        if (isDeleted) {
            return ResponseEntity.ok("User deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found.");
        }
    }
}
