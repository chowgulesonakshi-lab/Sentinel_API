CREATE TABLE incidents (
 id BIGSERIAL PRIMARY KEY, endpoint_id BIGINT NOT NULL REFERENCES endpoints(id) ON DELETE CASCADE,
 alert_id BIGINT REFERENCES alerts(id) ON DELETE SET NULL, title VARCHAR(180) NOT NULL, description TEXT,
 status VARCHAR(24) NOT NULL DEFAULT 'OPEN', severity VARCHAR(16) NOT NULL DEFAULT 'MEDIUM', priority VARCHAR(16) NOT NULL DEFAULT 'P2',
 assigned_to VARCHAR(255), created_by VARCHAR(255) NOT NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 acknowledged_at TIMESTAMP, investigating_at TIMESTAMP, resolved_at TIMESTAMP, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT chk_incident_status CHECK(status IN ('OPEN','ACKNOWLEDGED','INVESTIGATING','RESOLVED')),
 CONSTRAINT chk_incident_severity CHECK(severity IN ('CRITICAL','HIGH','MEDIUM','LOW')),
 CONSTRAINT chk_incident_priority CHECK(priority IN ('P1','P2','P3','P4'))
);
CREATE TABLE incident_events (
 id BIGSERIAL PRIMARY KEY, incident_id BIGINT NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
 actor_email VARCHAR(255) NOT NULL, event_type VARCHAR(32) NOT NULL, details TEXT, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_incident_endpoint_created ON incidents(endpoint_id,created_at DESC);
CREATE INDEX idx_incident_status ON incidents(status);
CREATE INDEX idx_incident_event_created ON incident_events(incident_id,created_at);