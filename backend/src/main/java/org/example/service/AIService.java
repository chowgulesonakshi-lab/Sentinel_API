package org.example.service;

import org.example.dto.AIAnalysisResponse;

public interface AIService {
    AIAnalysisResponse analyzeIncident(Long alertId);
}
