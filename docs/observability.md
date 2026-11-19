# Sentinel API Observability

## Request correlation

Every HTTP request receives an `X-Correlation-ID` response header. A valid incoming value is retained; otherwise the backend generates a UUID. The value is placed in SLF4J MDC for the lifetime of the request and is cleared when request processing finishes.

Use the returned header value when searching backend logs:

```text
X-Correlation-ID: 6b4f2a1d-4d4c-4d9a-8a3f-9f2d6d4b2c10
```

The correlation ID is distinct from the distributed trace ID.

## Distributed tracing

The backend uses Micrometer Tracing with the Brave bridge and Zipkin reporter. Spring Boot instruments inbound web requests, and monitored outbound HTTP requests are built with Spring Boot's `RestTemplateBuilder` so client spans can join the current trace.

Configuration is supplied through environment variables:

| Variable | Default | Purpose |
| --- | --- | --- |
| `TRACING_SAMPLING_PROBABILITY` | `0.1` | Fraction of requests sampled, from `0.0` to `1.0` |
| `ZIPKIN_ENDPOINT` | `http://localhost:9411/api/v2/spans` | Zipkin v2 span collector endpoint |

When Zipkin is not running, tracing remains available locally in the application, but spans cannot be exported to a collector. For a hosted environment, set `ZIPKIN_ENDPOINT` to a reachable collector URL.

Console logs include correlation, trace, and span identifiers when those values are available.

## Prometheus metrics

The Prometheus registry is exposed through:

```text
GET /actuator/prometheus
```

The endpoint remains protected by the application's existing Spring Security rules. Do not make it publicly accessible without an explicit authentication or network-access policy.

Custom metrics:

| Metric | Type | Tags |
| --- | --- | --- |
| `sentinel.monitoring.checks` | Counter | `method`, `outcome`, `status_code` |
| `sentinel.monitoring.response.time` | Timer | `method`, `outcome` |

The response-time timer publishes a histogram suitable for latency percentile calculations. Endpoint IDs and full URLs are intentionally excluded from metric labels to avoid high-cardinality metrics.

Standard Spring Boot HTTP server, JVM, and process metrics are also available through the Micrometer registry.

## Alert webhook notifications

Alert creation and resolution can be delivered to a generic HTTP(S) webhook. Delivery is asynchronous, uses a bounded executor and short HTTP timeouts, and does not block monitoring or alert persistence when the remote receiver is unavailable.

Configuration:

| Variable | Default | Purpose |
| --- | --- | --- |
| `ALERT_WEBHOOK_ENABLED` | `false` | Enable webhook delivery |
| `ALERT_WEBHOOK_URL` | empty | Public HTTP(S) receiver URL |

The receiver gets a JSON payload containing the alert ID, lifecycle event (`ALERT_CREATED` or `ALERT_RESOLVED`), alert type, severity, message, resolved state, and event timestamp. Endpoint URLs and user information are not included. The configured URL is validated to reject localhost, private, and reserved network targets. Keep any secret webhook URL in the deployment platform's secret/environment settings, never in source control.

