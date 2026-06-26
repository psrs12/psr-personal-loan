CREATE TABLE applicant
(
    applicant_id      UUID          NOT NULL,
    application_id    UUID          NOT NULL,
    first_name        VARCHAR(100)  NOT NULL,
    last_name         VARCHAR(100)  NOT NULL,
    date_of_birth     DATE          NOT NULL,
    citizenship       VARCHAR(50)   NOT NULL,
    ssn_encrypted     BYTEA         NOT NULL,
    email             VARCHAR(200)  NOT NULL,
    phone             VARCHAR(20)   NOT NULL,
    street            VARCHAR(200)  NOT NULL,
    city              VARCHAR(100)  NOT NULL,
    state             VARCHAR(2)    NOT NULL,
    zip               VARCHAR(10)   NOT NULL,
    employer_name     VARCHAR(200),
    employment_status VARCHAR(50),
    annual_income     DECIMAL(12, 2),
    created_timestamp TIMESTAMP     NOT NULL,
    updated_timestamp TIMESTAMP,

    CONSTRAINT pk_applicant PRIMARY KEY (applicant_id),
    CONSTRAINT fk_applicant_application FOREIGN KEY (application_id) REFERENCES application (application_id)
);

CREATE INDEX idx_applicant_application_id ON applicant (application_id);
