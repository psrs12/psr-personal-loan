# Persistence Model

# Document Service

Version: 1.0

---

## Database

PostgreSQL

## Migration

Flyway

## Schema

`document_service`

---

# Tables

## Table: document_requirement

One row per document type per application. Created when `FinalDecisionDocumentsRequired` is processed.

```sql
CREATE TABLE document_requirement
(
    requirement_id  UUID         PRIMARY KEY,
    application_id  UUID         NOT NULL,
    document_type   VARCHAR(50)  NOT NULL,
    count           INTEGER      NOT NULL,
    description     VARCHAR(255),
    status          VARCHAR(30)  NOT NULL,  -- PENDING | UPLOADED | REJECTED | COMPLETED
    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP    NOT NULL
);
```

---

## Table: document_record

One row per uploaded file. Multiple records may exist per requirement (re-uploads after rejection).

```sql
CREATE TABLE document_record
(
    document_id     UUID         PRIMARY KEY,
    application_id  UUID         NOT NULL,
    requirement_id  UUID         NOT NULL,
    document_type   VARCHAR(50)  NOT NULL,
    storage_ref     VARCHAR(500) NOT NULL,  -- S3 key
    status          VARCHAR(30)  NOT NULL,  -- UPLOADED | SCANNING | VERIFIED | REJECTED
    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP    NOT NULL,

    FOREIGN KEY (requirement_id)
        REFERENCES document_requirement (requirement_id)
);
```

`storage_ref` is VARCHAR(500) to accommodate full S3 key paths including prefix, applicationId, and filename.

---

# Indexes

## document_requirement

```
IDX_DOC_REQ_APPLICATION_ID                  ON document_requirement (application_id)
IDX_DOC_REQ_APPLICATION_ID_DOCUMENT_TYPE    ON document_requirement (application_id, document_type)
IDX_DOC_REQ_STATUS                          ON document_requirement (status)
```

## document_record

```
IDX_DOC_REC_APPLICATION_ID  ON document_record (application_id)
IDX_DOC_REC_REQUIREMENT_ID  ON document_record (requirement_id)
IDX_DOC_REC_STATUS          ON document_record (status)
```

---

# Data Retention

| Table | Retention | Reason |
|-------|-----------|--------|
| `document_requirement` | 7 years | Regulatory audit trail |
| `document_record` | 7 years | Regulatory audit trail |

Note: S3 objects should be independently configured with a matching 7-year retention policy via S3 lifecycle rules.

---

# Design Notes

- `DocumentJpaAdapter` implements both `DocumentRequirementRepository` and `DocumentRecordRepository` ports.
- `status` columns use VARCHAR rather than a DB enum to allow status values to be extended without a schema migration.
- `updated_at` is always set on every write — tracked at the JPA entity level.
- The ACL mapper (`DecisionEngineDocumentCodeMapper`) is an infrastructure component, not a JPA entity. It has no persistence.
