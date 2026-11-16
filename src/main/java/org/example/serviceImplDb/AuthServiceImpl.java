package org.example.serviceImplDb;

import lombok.RequiredArgsConstructor;
import org.example.dto.AppUserDTO;
import org.example.dto.LoginRequestDTO;
import org.example.dto.LoginResponseDTO;
import org.example.entity.AppUser;
import org.example.repository.AppUserRepository;
import org.example.security.JwtUtil;
import org.example.service.AuthService;
import org.example.service.DemoWorkspaceService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final DemoWorkspaceService demoWorkspaceService;

    @Override
    @Transactional
    public LoginResponseDTO registerUser(AppUserDTO appUserDTO) {
        String email = appUserDTO.getEmail().trim().toLowerCase();
        String name = appUserDTO.getName().trim();

        if (appUserRepository.existsByEmail(email)) {
            return new LoginResponseDTO(null, "Error: Email already registered");
        }
        if (appUserRepository.existsByName(name)) {
            return new LoginResponseDTO(null, "Error: Username already exists");
        }

        AppUser appUser = new AppUser();
        appUser.setName(name);
        appUser.setEmail(email);
        appUser.setPassword(passwordEncoder.encode(appUserDTO.getPassword()));
        // Public signup must never accept a caller-selected privileged role.
        appUser.setRole(AppUser.Role.VIEWER);

        AppUser savedUser = appUserRepository.save(appUser);
        demoWorkspaceService.createDemoWorkspace(savedUser);
        return new LoginResponseDTO(null, "Success");
    }

    @Override
    public LoginResponseDTO loginUser(LoginRequestDTO loginRequestDTO) {
        try {
            String email = loginRequestDTO.getEmail().trim().toLowerCase();
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, loginRequestDTO.getPassword()));
            return new LoginResponseDTO(jwtUtil.generateToken(email), "Success");
        } catch (BadCredentialsException e) {
            return new LoginResponseDTO(null, "Error: Invalid email or password");
        }
    }
}
