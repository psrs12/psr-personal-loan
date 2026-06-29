CREATE TABLE loan_request
(
    loan_request_id   UUID           NOT NULL,
    application_id    UUID           NOT NULL,
    requested_amount  DECIMAL(12, 2) NOT NULL,
    term_months       INTEGER        NOT NULL,
    loan_purpose      VARCHAR(100),
    created_timestamp TIMESTAMP      NOT NULL,
    updated_timestamp TIMESTAMP,

    CONSTRAINT pk_loan_request PRIMARY KEY (loan_request_id),
    CONSTRAINT fk_loan_request_application FOREIGN KEY (application_id) REFERENCES application (application_id)
);

CREATE INDEX idx_loan_request_application_id ON loan_request (application_id);
