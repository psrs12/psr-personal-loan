# Persistence Model

# Offer Acceptance Service

Version: 1.0

---

## Database

PostgreSQL

## Migration

Flyway

## Schema

`offer_acceptance`

---

# Tables

## Table: offer_acceptance_session

One row per application. Created when `FinalDecisionApproved` is received.

```sql
CREATE TABLE offer_acceptance_session
(
    session_id      UUID         PRIMARY KEY,
    application_id  UUID         NOT NULL UNIQUE,
    status          VARCHAR(20)  NOT NULL,   -- PENDING | SIGNED
    created_at      TIMESTAMP    NOT NULL
);
```

Unique constraint on `application_id` enforces one session per application and provides the idempotency guarantee on duplicate `FinalDecisionApproved` events.

---

## Table: esign_record

One row per application. Created when e-sign is captured. Immutable after insert.

```sql
CREATE TABLE esign_record
(
    esign_id                  UUID         PRIMARY KEY,
    application_id            UUID         NOT NULL,
    session_id                UUID         NOT NULL,
    accepted_declaration_ids  TEXT         NOT NULL,   -- JSON array of UUID strings
    ip_address                VARCHAR(45),
    signed_at                 TIMESTAMP    NOT NULL,

    FOREIGN KEY (session_id)
        REFERENCES offer_acceptance_session (session_id)
);
```

`accepted_declaration_ids` stores a JSON-serialised array of the accepted declaration UUIDs. Stored as TEXT because declarations are value objects without their own table.

`ip_address` is VARCHAR(45) to support both IPv4 and IPv6.

---

# Indexes

## offer_acceptance_session

```
UNIQUE idx_oas_application_id ON offer_acceptance_session (application_id)
```

## esign_record

```
IDX_ESIGN_APPLICATION_ID ON esign_record (application_id)
IDX_ESIGN_SESSION_ID     ON esign_record (session_id)
```

---

# Data Retention

| Table | Retention | Reason |
|-------|-----------|--------|
| `offer_acceptance_session` | 7 years | Regulatory audit — e-sign evidence |
| `esign_record` | 7 years | Regulatory audit — e-sign evidence; ESIGN Act |

---

# Design Notes

- Declarations are NOT persisted as rows. They are defined as a static list in `OfferAcceptanceSession.STANDARD_DECLARATIONS` and rehydrated in-memory from the application code on each load. Only the `accepted_declaration_ids` (the applicant's choices) are stored.
- The `OfferAcceptanceJpaAdapter` implements both `OfferAcceptanceSessionRepository` and `ESignRecordRepository` ports as a single adapter class.
