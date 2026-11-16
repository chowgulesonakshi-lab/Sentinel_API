package org.example.serviceImplDb;

import lombok.RequiredArgsConstructor;
import org.example.dto.AppUserDTO;
import org.example.dto.LoginRequestDTO;
import org.example.dto.LoginResponseDTO;
import org.example.entity.AppUser;
import org.example.security.JwtUtil;
import org.example.service.AuthService;
import org.example.service.DemoWorkspaceService;
import org.example.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final DemoWorkspaceService demoWorkspaceService;

    @Override
    public LoginResponseDTO registerUser(AppUserDTO appUserDTO) {
        String email = appUserDTO.getEmail() == null
                ? null : appUserDTO.getEmail().trim().toLowerCase(Locale.ROOT);

        if (userService.existsByUsername(appUserDTO.getName())) {
            return new LoginResponseDTO(null, "Error: Username already exists");
        }
        if (email == null || email.isBlank()) {
            return new LoginResponseDTO(null, "Error: Email is required");
        }
        if (userService.existsByEmail(email)) {
            return new LoginResponseDTO(null, "Error: Email already exists");
        }

        AppUser appUser = new AppUser();
        appUser.setName(appUserDTO.getName());
        appUser.setEmail(email);
        appUser.setPassword(passwordEncoder.encode(appUserDTO.getPassword()));
        // Public registration must never allow a client to grant itself privileges.
        appUser.setRole(AppUser.Role.VIEWER);

        AppUser savedUser = userService.saveUser(appUser);
        demoWorkspaceService.createDemoWorkspace(savedUser);
        return new LoginResponseDTO(null, "Success");
    }

    @Override
    public LoginResponseDTO loginUser(LoginRequestDTO loginRequestDTO) {
        try {
            String email = loginRequestDTO.getEmail().trim().toLowerCase(Locale.ROOT);
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, loginRequestDTO.getPassword()));
            String token = jwtUtil.generateToken(email);
            return new LoginResponseDTO(token, "Success");
        } catch (BadCredentialsException e) {
            return new LoginResponseDTO(null, "Error: Invalid Username or Password");
        }
    }
}
