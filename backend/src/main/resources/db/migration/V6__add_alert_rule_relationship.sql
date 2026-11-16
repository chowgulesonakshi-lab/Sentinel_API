-- Add the alert-rule relationship expected by the current Alerts entity.
-- Nullable to preserve existing alerts that were created before rule association existed.
ALTER TABLE alerts
    ADD COLUMN IF NOT EXISTS rule_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_alerts_rule'
          AND conrelid = 'alerts'::regclass
    ) THEN
        ALTER TABLE alerts
            ADD CONSTRAINT fk_alerts_rule
            FOREIGN KEY (rule_id) REFERENCES alert_rules(id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_alerts_rule_id ON alerts(rule_id);
