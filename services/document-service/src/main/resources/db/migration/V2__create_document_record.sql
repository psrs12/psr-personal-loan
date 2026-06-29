CREATE TABLE document_record (
    document_id    UUID         NOT NULL,
    application_id UUID         NOT NULL,
    requirement_id UUID         NOT NULL,
    document_type  VARCHAR(50)  NOT NULL,
    storage_ref    VARCHAR(500) NOT NULL,
    status         VARCHAR(30)  NOT NULL,
    created_at     TIMESTAMP    NOT NULL,
    updated_at     TIMESTAMP    NOT NULL,
    CONSTRAINT pk_document_record PRIMARY KEY (document_id),
    CONSTRAINT fk_document_requirement FOREIGN KEY (requirement_id)
        REFERENCES document_requirement (requirement_id)
);

CREATE INDEX idx_record_application_id ON document_record (application_id);
CREATE INDEX idx_record_document_type  ON document_record (application_id, document_type);
