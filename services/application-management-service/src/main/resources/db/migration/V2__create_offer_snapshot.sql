CREATE TABLE offer_snapshot
(
    snapshot_id           UUID           NOT NULL,
    session_id            UUID           NOT NULL,
    offer_id              VARCHAR(100),
    customer_reference_id VARCHAR(100),
    loan_amount           DECIMAL(12, 2),
    apr                   DECIMAL(5, 2),
    term_months           INTEGER,
    expiration_date       DATE,
    retrieved_timestamp   TIMESTAMP      NOT NULL,

    CONSTRAINT pk_offer_snapshot PRIMARY KEY (snapshot_id),
    CONSTRAINT fk_offer_snapshot_session FOREIGN KEY (session_id) REFERENCES invitation_session (session_id)
);

CREATE INDEX idx_offer_snapshot_session_id ON offer_snapshot (session_id);
