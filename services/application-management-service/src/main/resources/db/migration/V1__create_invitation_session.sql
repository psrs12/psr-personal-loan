CREATE TABLE invitation_session
(
    session_id            UUID          NOT NULL,
    invitation_id         VARCHAR(100)  NOT NULL,
    application_source    VARCHAR(50)   NOT NULL,
    offer_id              VARCHAR(100),
    customer_reference_id VARCHAR(100),
    status                VARCHAR(50)   NOT NULL,
    created_timestamp     TIMESTAMP     NOT NULL,
    updated_timestamp     TIMESTAMP,
    expiration_timestamp  TIMESTAMP     NOT NULL,

    CONSTRAINT pk_invitation_session PRIMARY KEY (session_id)
);

CREATE INDEX idx_invitation_session_invitation_id ON invitation_session (invitation_id);
CREATE INDEX idx_invitation_session_status ON invitation_session (status);
