CREATE TABLE esign_record (
    esign_id                  UUID         NOT NULL,
    application_id            UUID         NOT NULL,
    session_id                UUID         NOT NULL,
    accepted_declaration_ids  TEXT         NOT NULL,
    ip_address                VARCHAR(45),
    signed_at                 TIMESTAMP    NOT NULL,
    CONSTRAINT pk_esign_record PRIMARY KEY (esign_id),
    CONSTRAINT fk_esign_session FOREIGN KEY (session_id)
        REFERENCES offer_acceptance_session (session_id)
);
