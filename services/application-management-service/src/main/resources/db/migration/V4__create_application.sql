CREATE TABLE application
(
    application_id     UUID         NOT NULL,
    intake_id          UUID,
    application_source VARCHAR(50)  NOT NULL,
    application_status VARCHAR(50)  NOT NULL,
    created_timestamp  TIMESTAMP    NOT NULL,
    updated_timestamp  TIMESTAMP,

    CONSTRAINT pk_application PRIMARY KEY (application_id),
    CONSTRAINT fk_application_intake FOREIGN KEY (intake_id) REFERENCES application_intake_context (intake_id)
);

CREATE INDEX idx_application_intake_id ON application (intake_id);
CREATE INDEX idx_application_status ON application (application_status);
CREATE INDEX idx_application_source ON application (application_source);
