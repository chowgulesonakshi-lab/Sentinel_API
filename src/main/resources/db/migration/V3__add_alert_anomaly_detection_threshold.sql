-- Bring the pre-existing alerts table in line with the current Alert entity.
-- Nullable because historical alert rows have no value for this newer field.
ALTER TABLE alerts
    ADD COLUMN IF NOT EXISTS anomaly_detection_threshold DOUBLE PRECISION;
