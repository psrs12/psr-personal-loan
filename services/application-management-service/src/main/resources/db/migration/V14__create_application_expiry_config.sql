CREATE TABLE application_expiry_config
(
    id                    UUID        NOT NULL,
    product_type          VARCHAR(50) NOT NULL,
    channel               VARCHAR(50) NOT NULL,
    expiry_threshold_days INTEGER     NOT NULL,

    CONSTRAINT pk_application_expiry_config PRIMARY KEY (id),
    CONSTRAINT uq_expiry_config_product_channel UNIQUE (product_type, channel)
);

INSERT INTO application_expiry_config (id, product_type, channel, expiry_threshold_days)
VALUES (gen_random_uuid(), 'PERSONAL_LOAN', 'INVITATION', 30),
       (gen_random_uuid(), 'PERSONAL_LOAN', 'DIRECT', 30);
