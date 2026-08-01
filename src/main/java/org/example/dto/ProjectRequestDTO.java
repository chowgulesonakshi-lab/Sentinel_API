package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProjectRequestDTO {
    @NotBlank(message = "Name cannot be empty")
    private String name;

    @NotBlank(message = "Description cannot be empty")
    private String description;

}
