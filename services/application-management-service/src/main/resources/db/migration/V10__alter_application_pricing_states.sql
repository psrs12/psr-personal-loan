ALTER TABLE application
    ADD COLUMN soft_pull_credit_report_reference_id VARCHAR(200),
    ADD COLUMN hard_pull_credit_report_reference_id VARCHAR(200),
    ADD COLUMN campaign_offer_id                    VARCHAR(100),
    ADD COLUMN campaign_offer_terms                 JSONB,
    ADD COLUMN application_expiry_date              TIMESTAMP;
