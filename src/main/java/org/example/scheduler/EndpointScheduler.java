package org.example.scheduler;

import org.example.service.MonitoringService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EndpointScheduler {
    private final MonitoringService monitoringService;

    public EndpointScheduler(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @Scheduled(fixedDelay = 5000)
    public void monitorEndpoints(){
        monitoringService.monitorDueEndpoints();
    }
}
