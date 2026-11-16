-- Complete the anomaly metadata columns expected by the current Alert entity.
-- IF NOT EXISTS keeps this safe for databases that already have some of these fields.
ALTER TABLE alerts
    ADD COLUMN IF NOT EXISTS anomaly_deviation_from_average DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS anomaly_sensitivity_multiplier DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS anomaly_explanation TEXT;
