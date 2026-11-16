package org.example.service;

import org.example.dto.AppUserDTO;
import org.example.dto.LoginRequestDTO;
import org.example.dto.LoginResponseDTO;

public interface AuthService {
    LoginResponseDTO registerUser(AppUserDTO appUserDTO);
    LoginResponseDTO loginUser(LoginRequestDTO loginRequestDTO);
}
