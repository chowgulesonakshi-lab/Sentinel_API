-- Reconcile every anomaly metadata field mapped by org.example.entity.Alerts.
-- Nullable columns preserve historical alert records and allow older alerts to remain valid.
ALTER TABLE alerts
    ADD COLUMN IF NOT EXISTS anomaly_observed_response_time DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS anomaly_historical_average DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS anomaly_standard_deviation DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS anomaly_detection_threshold DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS anomaly_deviation_from_average DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS anomaly_sensitivity_multiplier DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS anomaly_explanation TEXT;
