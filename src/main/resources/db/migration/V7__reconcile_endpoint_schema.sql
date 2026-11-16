-- Reconcile the complete endpoints table with the current Endpoint entity.
-- Existing databases baselined at V1 may have skipped the original table definition.
-- Every addition is idempotent and preserves existing endpoint data.

ALTER TABLE endpoints
    ADD COLUMN IF NOT EXISTS method VARCHAR(255),
    ADD COLUMN IF NOT EXISTS url VARCHAR(255),
    ADD COLUMN IF NOT EXISTS timeout_millis INTEGER,
    ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS interval_seconds INTEGER,
    ADD COLUMN IF NOT EXISTS expected_status_codes VARCHAR(255),
    ADD COLUMN IF NOT EXISTS slow_response_threshold_millis INTEGER,
    ADD COLUMN IF NOT EXISTS consecutive_failures_threshold INTEGER,
    ADD COLUMN IF NOT EXISTS consecutive_failures_count INTEGER,
    ADD COLUMN IF NOT EXISTS anomaly_detection_enabled BOOLEAN,
    ADD COLUMN IF NOT EXISTS anomaly_sensitivity_multiplier DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS anomaly_observation_window INTEGER,
    ADD COLUMN IF NOT EXISTS anomaly_minimum_observations INTEGER,
    ADD COLUMN IF NOT EXISTS headers TEXT,
    ADD COLUMN IF NOT EXISTS request_body TEXT,
    ADD COLUMN IF NOT EXISTS last_monitored_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS service_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_endpoints_service'
          AND conrelid = 'endpoints'::regclass
    ) THEN
        ALTER TABLE endpoints
            ADD CONSTRAINT fk_endpoints_service
            FOREIGN KEY (service_id) REFERENCES api_service(id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_endpoint_service_id ON endpoints(service_id);
