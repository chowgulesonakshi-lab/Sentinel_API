package org.example.service;

import org.example.entity.Endpoint;
import org.example.entity.Metrics;

public interface MonitoringService {
    void monitorAllEndpoints();
    void monitorDueEndpoints();
    Metrics monitorEndpoint(Endpoint endpoint);
    Metrics monitorEndpointById(Long endpointId);
}
