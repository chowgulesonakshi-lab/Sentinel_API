-- Add tables that were absent from databases baselined at Flyway version 1.
-- Existing application tables and data are left untouched.

CREATE TABLE IF NOT EXISTS alert_rules (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    condition_type VARCHAR(255) NOT NULL,
    condition_parameters TEXT,
    severity VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    endpoint_id BIGINT REFERENCES endpoints(id),
    service_id BIGINT REFERENCES api_service(id),
    evaluation_window_minutes INTEGER,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS service_dependencies (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES project(id),
    dependent_service_id BIGINT NOT NULL REFERENCES api_service(id),
    dependency_service_id BIGINT NOT NULL REFERENCES api_service(id),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_service_dependency
        UNIQUE (project_id, dependent_service_id, dependency_service_id)
);

CREATE INDEX IF NOT EXISTS idx_alert_rules_endpoint_id
    ON alert_rules(endpoint_id);

CREATE INDEX IF NOT EXISTS idx_alert_rules_service_id
    ON alert_rules(service_id);

CREATE INDEX IF NOT EXISTS idx_service_dependencies_project_id
    ON service_dependencies(project_id);
