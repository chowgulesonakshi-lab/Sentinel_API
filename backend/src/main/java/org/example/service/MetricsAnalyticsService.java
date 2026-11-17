package org.example.service;

import org.example.dto.MetricsAnalyticsDTO;
import org.example.dto.MetricsResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface MetricsAnalyticsService {
    MetricsAnalyticsDTO getAnalytics(Long endpointId, LocalDateTime from, LocalDateTime to);
    Page<MetricsResponseDTO> getEndpointHistory(Long endpointId, LocalDateTime from,
                                                LocalDateTime to, Pageable pageable);
}
