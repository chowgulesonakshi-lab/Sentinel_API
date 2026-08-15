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
        if (userService.existsByUsername(appUserDTO.getName())) {
            return new LoginResponseDTO(null, "Error: Username already exists");
        }
        AppUser appUser = new AppUser();
        appUser.setName(appUserDTO.getName());
        appUser.setEmail(appUserDTO.getEmail());
        appUser.setPassword(passwordEncoder.encode(appUserDTO.getPassword()));
        appUser.setRole(appUserDTO.getRole());

        AppUser savedUser = userService.saveUser(appUser);
        demoWorkspaceService.createDemoWorkspace(savedUser);
        return new LoginResponseDTO(null, "Success");
    }

    @Override
    public LoginResponseDTO loginUser(LoginRequestDTO loginRequestDTO) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequestDTO.getEmail(),
                            loginRequestDTO.getPassword()));
            String token = jwtUtil.generateToken(loginRequestDTO.getEmail());
            return new LoginResponseDTO(token, "Success");
        } catch (BadCredentialsException e) {
            return new LoginResponseDTO(null,
                    "Error: Invalid Username or Password");
        }
    }
}
