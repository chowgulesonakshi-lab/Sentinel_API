package org.example.dto;

import lombok.Data;

import java.util.List;

@Data
public class AIAnalysisResponse {
    private String severity;
    private String summary;
    private String likelyCause;
    private List<String> possibleCauses;
    private String recommendation;
    private Integer confidence;
}
