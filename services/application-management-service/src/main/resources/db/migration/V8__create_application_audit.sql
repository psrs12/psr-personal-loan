CREATE TABLE application_audit
(
    audit_id          UUID          NOT NULL,
    application_id    UUID,
    intake_id         UUID,
    event_type        VARCHAR(100)  NOT NULL,
    event_timestamp   TIMESTAMP     NOT NULL,
    payload           JSONB,

    CONSTRAINT pk_application_audit PRIMARY KEY (audit_id)
);

CREATE INDEX idx_audit_application_id ON application_audit (application_id);
CREATE INDEX idx_audit_event_type ON application_audit (event_type);
CREATE INDEX idx_audit_event_timestamp ON application_audit (event_timestamp);
