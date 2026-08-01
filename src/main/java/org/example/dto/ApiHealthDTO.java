package org.example.dto;

import lombok.Data;

@Data
public class ApiHealthDTO {
    private Long id;
    private String name;
    private String url;
    private String status;
    private Double latency;
    private Integer statusCode;
}
