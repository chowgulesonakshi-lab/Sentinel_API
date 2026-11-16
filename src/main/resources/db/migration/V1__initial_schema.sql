-- Initial Sentinel API schema.
-- Existing non-empty databases are baselined at version 1 before this migration runs.
-- New databases receive this schema and are subsequently checked by Hibernate validate.

CREATE TABLE app_user (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    role VARCHAR(32)
);

CREATE TABLE project (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(255),
    created_at DATE,
    owner_id BIGINT REFERENCES app_user(id)
);

CREATE TABLE api_service (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(255),
    project_id BIGINT REFERENCES project(id),
    status VARCHAR(32)
);

CREATE TABLE endpoints (
    id BIGSERIAL PRIMARY KEY,
    method VARCHAR(16),
    url VARCHAR(255),
    timeout_millis INTEGER,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    interval_seconds INTEGER,
    expected_status_codes VARCHAR(255),
    slow_response_threshold_millis INTEGER,
    consecutive_failures_threshold INTEGER,
    consecutive_failures_count INTEGER,
    anomaly_detection_enabled BOOLEAN,
    anomaly_sensitivity_multiplier DOUBLE PRECISION,
    anomaly_observation_window INTEGER,
    anomaly_minimum_observations INTEGER,
    headers TEXT,
    request_body TEXT,
    last_monitored_at TIMESTAMP,
    service_id BIGINT REFERENCES api_service(id)
);

CREATE TABLE alert_rules (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    condition_type VARCHAR(64) NOT NULL,
    condition_parameters TEXT,
    severity VARCHAR(32) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    endpoint_id BIGINT REFERENCES endpoints(id),
    service_id BIGINT REFERENCES api_service(id),
    evaluation_window_minutes INTEGER,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE metrics (
    id BIGSERIAL PRIMARY KEY,
    response_time DOUBLE PRECISION,
    success BOOLEAN NOT NULL DEFAULT FALSE,
    status_code INTEGER,
    time_stamp TIMESTAMP,
    endpoint_id BIGINT REFERENCES endpoints(id),
    status VARCHAR(16)
);

CREATE TABLE alerts (
    id BIGSERIAL PRIMARY KEY,
    endpoint_id BIGINT REFERENCES endpoints(id),
    rule_id BIGINT REFERENCES alert_rules(id),
    severity VARCHAR(32),
    type VARCHAR(64),
    message VARCHAR(255),
    resolved BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP,
    resolved_at TIMESTAMP,
    anomaly_observed_response_time DOUBLE PRECISION,
    anomaly_historical_average DOUBLE PRECISION,
    anomaly_standard_deviation DOUBLE PRECISION,
    anomaly_detection_threshold DOUBLE PRECISION,
    anomaly_deviation_from_average DOUBLE PRECISION,
    anomaly_sensitivity_multiplier DOUBLE PRECISION,
    anomaly_explanation TEXT
);

CREATE TABLE service_dependencies (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES project(id),
    dependent_service_id BIGINT NOT NULL REFERENCES api_service(id),
    dependency_service_id BIGINT NOT NULL REFERENCES api_service(id),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_service_dependency
        UNIQUE (project_id, dependent_service_id, dependency_service_id)
);

CREATE INDEX idx_project_owner_id ON project(owner_id);
CREATE INDEX idx_api_service_project_id ON api_service(project_id);
CREATE INDEX idx_endpoint_service_id ON endpoints(service_id);
CREATE INDEX idx_metrics_endpoint_timestamp ON metrics(endpoint_id, time_stamp DESC);
CREATE INDEX idx_alerts_endpoint_resolved_created ON alerts(endpoint_id, resolved, created_at DESC);
CREATE INDEX idx_alert_rules_endpoint_id ON alert_rules(endpoint_id);
CREATE INDEX idx_alert_rules_service_id ON alert_rules(service_id);
CREATE INDEX idx_service_dependencies_project_id ON service_dependencies(project_id);
