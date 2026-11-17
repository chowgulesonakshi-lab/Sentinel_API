package org.example.service;

import org.example.dto.SloReportDTO;
import org.example.dto.SloTargetRequestDTO;

public interface SloService {
    SloReportDTO updateTarget(Long endpointId, SloTargetRequestDTO request);
    SloReportDTO getReport(Long endpointId);
}
