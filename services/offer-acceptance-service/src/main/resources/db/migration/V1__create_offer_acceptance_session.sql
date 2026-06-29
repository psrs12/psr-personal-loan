CREATE TABLE offer_acceptance_session (
    session_id     UUID        NOT NULL,
    application_id UUID        NOT NULL,
    status         VARCHAR(20) NOT NULL,
    created_at     TIMESTAMP   NOT NULL,
    CONSTRAINT pk_offer_acceptance_session PRIMARY KEY (session_id),
    CONSTRAINT uq_session_application_id UNIQUE (application_id)
);
