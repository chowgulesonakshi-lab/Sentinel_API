package org.example.dto;

import lombok.Data;
import org.example.entity.AppUser;

@Data
public class AppUserDTO {
    private String name;
    private String email;
    private String password;
    private AppUser.Role role;
}