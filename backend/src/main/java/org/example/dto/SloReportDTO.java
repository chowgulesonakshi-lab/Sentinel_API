package org.example.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SloReportDTO {
    private Long endpointId;
    private String endpointUrl;
    private double targetPercentage;
    private LocalDateTime generatedAt;
    private List<SloPeriodReportDTO> periods;
}
