package org.example.service;

import org.example.entity.Endpoint;

public interface MonitoringService {
    void monitorAllEndpoints();
    void monitorEndpoint(Endpoint endpoint);
}
