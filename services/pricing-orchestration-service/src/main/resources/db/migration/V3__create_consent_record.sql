CREATE TABLE consent_record
(
    id                        UUID         NOT NULL,
    application_id            UUID         NOT NULL,
    consent_type              VARCHAR(50)  NOT NULL,
    consent_given_at          TIMESTAMP    NOT NULL,
    consent_channel           VARCHAR(50)  NOT NULL,
    applicant_reference       VARCHAR(200),
    selected_pricing_offer_id UUID,

    CONSTRAINT pk_consent_record PRIMARY KEY (id)
);

CREATE INDEX idx_consent_record_application_id ON consent_record (application_id);
CREATE INDEX idx_consent_record_type ON consent_record (consent_type);
