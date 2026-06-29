CREATE TABLE document_requirement (
    requirement_id  UUID         NOT NULL,
    application_id  UUID         NOT NULL,
    document_type   VARCHAR(50)  NOT NULL,
    count           INTEGER      NOT NULL,
    description     VARCHAR(255),
    status          VARCHAR(30)  NOT NULL,
    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP    NOT NULL,
    CONSTRAINT pk_document_requirement PRIMARY KEY (requirement_id),
    CONSTRAINT uq_requirement_app_type UNIQUE (application_id, document_type)
);

CREATE INDEX idx_requirement_application_id ON document_requirement (application_id);
