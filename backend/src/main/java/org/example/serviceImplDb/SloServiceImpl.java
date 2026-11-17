package org.example.serviceImplDb;

import lombok.RequiredArgsConstructor;
import org.example.dto.SloPeriodReportDTO;
import org.example.dto.SloReportDTO;
import org.example.dto.SloTargetRequestDTO;
import org.example.entity.Endpoint;
import org.example.entity.Metrics;
import org.example.entity.SloConfiguration;
import org.example.repository.EndpointRepository;
import org.example.repository.MetricRepository;
import org.example.repository.SloConfigurationRepository;
import org.example.service.SloService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SloServiceImpl implements SloService {
    private static final Set<Double> SUPPORTED_TARGETS = Set.of(99.0, 99.9, 99.95);
    private static final List<Integer> WINDOWS = List.of(7, 30, 90);

    private final EndpointRepository endpointRepository;
    private final MetricRepository metricRepository;
    private final SloConfigurationRepository sloConfigurationRepository;

    @Override
    @Transactional
    public SloReportDTO updateTarget(Long endpointId, SloTargetRequestDTO request) {
        if (request == null || request.getTargetPercentage() == null
                || !SUPPORTED_TARGETS.contains(request.getTargetPercentage())) {
            throw new IllegalArgumentException("SLO target must be one of 99.0, 99.9, or 99.95");
        }
        String email = currentUserEmail();
        Endpoint endpoint = endpointRepository
                .findByIdAndApiService_Project_Owner_EmailIgnoreCase(endpointId, email)
                .orElseThrow(() -> new AccessDeniedException("Endpoint not found or access denied"));

        SloConfiguration configuration = sloConfigurationRepository
                .findByEndpoint_IdAndEndpoint_ApiService_Project_Owner_EmailIgnoreCase(endpointId, email)
                .orElseGet(() -> {
                    SloConfiguration created = new SloConfiguration();
                    created.setEndpoint(endpoint);
                    return created;
                });
        configuration.setTargetPercentage(request.getTargetPercentage());
        sloConfigurationRepository.save(configuration);
        return buildReport(endpoint, configuration.getTargetPercentage());
    }

    @Override
    @Transactional(readOnly = true)
    public SloReportDTO getReport(Long endpointId) {
        String email = currentUserEmail();
        Endpoint endpoint = endpointRepository
                .findByIdAndApiService_Project_Owner_EmailIgnoreCase(endpointId, email)
                .orElseThrow(() -> new AccessDeniedException("Endpoint not found or access denied"));
        double target = sloConfigurationRepository
                .findByEndpoint_IdAndEndpoint_ApiService_Project_Owner_EmailIgnoreCase(endpointId, email)
                .map(SloConfiguration::getTargetPercentage)
                .orElse(99.9);
        return buildReport(endpoint, target);
    }

    private SloReportDTO buildReport(Endpoint endpoint, double target) {
        LocalDateTime now = LocalDateTime.now();
        SloReportDTO report = new SloReportDTO();
        report.setEndpointId(endpoint.getId());
        report.setEndpointUrl(endpoint.getUrl());
        report.setTargetPercentage(target);
        report.setGeneratedAt(now);
        report.setPeriods(WINDOWS.stream().map(days -> buildPeriod(endpoint.getId(), target, days, now)).toList());
        return report;
    }

    private SloPeriodReportDTO buildPeriod(Long endpointId, double target, int days, LocalDateTime now) {
        LocalDateTime from = now.minusDays(days);
        List<Metrics> metrics = metricRepository
                .findByEndpoint_ApiService_Project_Owner_EmailIgnoreCaseAndEndpoint_IdAndTimeStampBetweenOrderByTimeStampDesc(
                        currentUserEmail(), endpointId, from, now);
        long total = metrics.size();
        long successful = metrics.stream().filter(Metrics::isSuccess).count();
        long failed = total - successful;
        double availability = total == 0 ? 0.0 : successful * 100.0 / total;
        double allowedFailures = total * (100.0 - target) / 100.0;
        double consumed = allowedFailures == 0.0 ? 0.0 : failed * 100.0 / allowedFailures;
        double remaining = Math.max(0.0, 100.0 - consumed);
        double observedErrorRate = total == 0 ? 0.0 : failed * 100.0 / total;
        double permittedErrorRate = 100.0 - target;
        double burnRate = permittedErrorRate == 0.0 ? 0.0 : observedErrorRate / permittedErrorRate;

        SloPeriodReportDTO dto = new SloPeriodReportDTO();
        dto.setWindowDays(days);
        dto.setFrom(from);
        dto.setTo(now);
        dto.setTotalChecks(total);
        dto.setSuccessfulChecks(successful);
        dto.setFailedChecks(failed);
        dto.setTargetPercentage(target);
        dto.setAvailabilityPercentage(availability);
        dto.setAllowedFailedChecks(allowedFailures);
        dto.setErrorBudgetConsumedPercentage(consumed);
        dto.setErrorBudgetRemainingPercentage(remaining);
        dto.setBurnRate(burnRate);
        dto.setCompliant(total == 0 ? null : availability >= target);
        dto.setStatus(total == 0 ? "NO_DATA" : availability >= target ? "MEETING_SLO" : "BREACHED");
        return dto;
    }

    private String currentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
            throw new AccessDeniedException("Authentication is required");
        }
        return authentication.getName();
    }
}
