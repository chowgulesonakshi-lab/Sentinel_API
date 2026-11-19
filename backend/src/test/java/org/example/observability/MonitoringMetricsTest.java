package org.example.observability;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.example.entity.Endpoint;
import org.example.entity.Metrics;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MonitoringMetricsTest {

    @Test
    void recordsCheckCountAndResponseTimeWithLowCardinalityTags() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        MonitoringMetrics monitoringMetrics = new MonitoringMetrics(registry);

        Endpoint endpoint = new Endpoint();
        endpoint.setMethod(Endpoint.HttpMethod.GET);

        Metrics metrics = new Metrics();
        metrics.setSuccess(true);
        metrics.setStatusCode(200);
        metrics.setResponseTime(125.0);

        monitoringMetrics.recordCheck(endpoint, metrics);

        assertEquals(1.0, registry.get("sentinel.monitoring.checks")
                .tag("method", "GET")
                .tag("outcome", "success")
                .tag("status_code", "200")
                .counter()
                .count());

        assertEquals(1L, registry.get("sentinel.monitoring.response.time")
                .tag("method", "GET")
                .tag("outcome", "success")
                .timer()
                .count());

        assertEquals(0.125, registry.get("sentinel.monitoring.response.time")
                .tag("method", "GET")
                .tag("outcome", "success")
                .timer()
                .totalTime(java.util.concurrent.TimeUnit.SECONDS), 0.000001);
    }
}
