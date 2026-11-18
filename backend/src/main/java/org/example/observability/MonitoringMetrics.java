package org.example.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.example.entity.Endpoint;
import org.example.entity.Metrics;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class MonitoringMetrics {

    private final MeterRegistry meterRegistry;

    public MonitoringMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordCheck(Endpoint endpoint, Metrics metrics) {
        String method = endpoint != null && endpoint.getMethod() != null
                ? endpoint.getMethod().name()
                : "UNKNOWN";
        String outcome = metrics.isSuccess() ? "success" : "failure";
        String statusCode = metrics.getStatusCode() == null
                ? "unknown"
                : metrics.getStatusCode().toString();

        meterRegistry.counter(
                "sentinel.monitoring.checks",
                "method", method,
                "outcome", outcome,
                "status_code", statusCode
        ).increment();

        if (metrics.getResponseTime() != null && metrics.getResponseTime() >= 0) {
            Timer.builder("sentinel.monitoring.response.time")
                    .description("Response time of monitored endpoints")
                    .tags("method", method, "outcome", outcome)
                    .publishPercentileHistogram()
                    .register(meterRegistry)
                    .record(Math.round(metrics.getResponseTime() * 1_000_000d), TimeUnit.NANOSECONDS);
        }
    }
}
