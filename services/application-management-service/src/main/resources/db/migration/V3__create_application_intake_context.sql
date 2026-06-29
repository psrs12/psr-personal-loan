CREATE TABLE application_intake_context
(
    intake_id             UUID         NOT NULL,
    session_id            UUID,
    application_source    VARCHAR(50)  NOT NULL,
    invitation_id         VARCHAR(100),
    offer_id              VARCHAR(100),
    customer_reference_id VARCHAR(100),
    prefill_status        VARCHAR(50)  NOT NULL,
    created_timestamp     TIMESTAMP    NOT NULL,

    CONSTRAINT pk_application_intake_context PRIMARY KEY (intake_id),
    CONSTRAINT fk_intake_context_session FOREIGN KEY (session_id) REFERENCES invitation_session (session_id)
);

CREATE INDEX idx_intake_context_invitation_id ON application_intake_context (invitation_id);
CREATE INDEX idx_intake_context_source ON application_intake_context (application_source);
CREATE INDEX idx_intake_context_session_id ON application_intake_context (session_id);
