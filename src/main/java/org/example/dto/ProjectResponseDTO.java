package org.example.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ProjectResponseDTO {
    private Long id;
    private String name;
    private String description;

    private Long ownerId;
    private String ownerName;

    private LocalDate createdAt;
}
