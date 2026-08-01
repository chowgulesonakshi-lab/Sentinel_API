package org.example.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.dto.ApiHealthDTO;
import org.example.dto.DashboardResponseDTO;
import org.example.dto.MetricsResponseDTO;
import org.example.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Dashboard")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping
   public ResponseEntity<DashboardResponseDTO> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboardSummary());
    }

    @GetMapping("/recent-metrics")
    public ResponseEntity<List<MetricsResponseDTO>> getRecentMetrics() {
        return ResponseEntity.ok(dashboardService.getRecentMetrics());
    }

    @GetMapping("/health")
    public ResponseEntity<List<ApiHealthDTO>> getHealth(){
        return ResponseEntity.ok(dashboardService.getApiHealth());
    }

}
