CREATE TABLE pricing_offer
(
    pricing_offer_id     UUID           NOT NULL,
    application_id       UUID           NOT NULL,
    approved_amount      DECIMAL(12, 2) NOT NULL,
    interest_rate        DECIMAL(6, 4)  NOT NULL,
    apr                  DECIMAL(6, 4)  NOT NULL,
    term_months          INTEGER        NOT NULL,
    monthly_repayment    DECIMAL(12, 2) NOT NULL,
    total_repayable      DECIMAL(12, 2) NOT NULL,
    offer_expiry_date    TIMESTAMP      NOT NULL,
    pricing_model_ref    VARCHAR(200),
    bureau_snapshot_ref  VARCHAR(200),
    offer_status         VARCHAR(50)    NOT NULL,
    created_at           TIMESTAMP      NOT NULL,

    CONSTRAINT pk_pricing_offer PRIMARY KEY (pricing_offer_id)
);

CREATE INDEX idx_pricing_offer_application_id ON pricing_offer (application_id);
CREATE INDEX idx_pricing_offer_status ON pricing_offer (offer_status);
