CREATE TABLE application_offer
(
    application_offer_id  UUID           NOT NULL,
    application_id        UUID           NOT NULL,
    offer_id              VARCHAR(100)   NOT NULL,
    customer_reference_id VARCHAR(100)   NOT NULL,
    loan_amount           DECIMAL(12, 2) NOT NULL,
    apr                   DECIMAL(5, 2)  NOT NULL,
    term_months           INTEGER        NOT NULL,
    expiration_date       DATE,
    captured_timestamp    TIMESTAMP      NOT NULL,

    CONSTRAINT pk_application_offer PRIMARY KEY (application_offer_id),
    CONSTRAINT fk_application_offer_application FOREIGN KEY (application_id) REFERENCES application (application_id)
);

CREATE INDEX idx_application_offer_application_id ON application_offer (application_id);
