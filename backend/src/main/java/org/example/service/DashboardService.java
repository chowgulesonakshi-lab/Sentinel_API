package org.example.service;

import org.example.dto.ApiHealthDTO;
import org.example.dto.DashboardResponseDTO;
import org.example.dto.MetricsResponseDTO;
import org.example.entity.Metrics;

import java.util.List;

public interface DashboardService {
    DashboardResponseDTO getDashboardSummary();
    List<MetricsResponseDTO> getRecentMetrics();
    List<ApiHealthDTO> getApiHealth();
}
