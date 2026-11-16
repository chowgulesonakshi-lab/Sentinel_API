package org.example.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.dto.AppUserDTO;
import org.example.dto.LoginRequestDTO;
import org.example.dto.LoginResponseDTO;
import org.example.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authorization")
@RestController
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<LoginResponseDTO> signup(@RequestBody AppUserDTO appUserDTO){
        LoginResponseDTO response = authService.registerUser(appUserDTO);
        if("Success".equals(response.getMessage())){
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> signup(@RequestBody LoginRequestDTO loginRequestDTO){
        LoginResponseDTO response = authService.loginUser(loginRequestDTO);
        if("Success".equals(response.getMessage())){
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
