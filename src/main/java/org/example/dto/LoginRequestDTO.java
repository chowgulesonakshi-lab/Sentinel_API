package org.example.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequestDTO {
    @Email(message = "Invalid email")
    @NotBlank
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
