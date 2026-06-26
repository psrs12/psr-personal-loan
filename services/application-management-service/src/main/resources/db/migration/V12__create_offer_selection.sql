CREATE TABLE offer_selection
(
    id                       UUID      NOT NULL,
    application_id           UUID      NOT NULL,
    selected_pricing_offer_id UUID     NOT NULL,
    offer_selected_timestamp TIMESTAMP NOT NULL,

    CONSTRAINT pk_offer_selection PRIMARY KEY (id),
    CONSTRAINT fk_offer_selection_application FOREIGN KEY (application_id) REFERENCES application (application_id),
    CONSTRAINT fk_offer_selection_pricing_offer FOREIGN KEY (selected_pricing_offer_id) REFERENCES pricing_offer (pricing_offer_id),
    CONSTRAINT uq_offer_selection_application UNIQUE (application_id)
);
