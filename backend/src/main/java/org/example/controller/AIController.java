package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.AIAnalysisResponse;
import org.example.service.AIService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class AIController {
    private final AIService aiService;

    @PostMapping("/analyze/{alertId}")
    public AIAnalysisResponse analyzeIncident(@PathVariable Long alertId){
        return aiService.analyzeIncident(alertId);
    }
}
