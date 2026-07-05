# Persistence Model

## Database

PostgreSQL

## Purpose

Persist invitation processing state, intake context, application lifecycle,
offer terms, applicant information, and audit history.

---

# Intake Tables

## Table: invitation_session

Tracks the multi-step invitation validation process with external systems.
Linked to intake context. Retained for 12 months.

```sql
CREATE TABLE invitation_session
(
 session_id            UUID          PRIMARY KEY,
 invitation_id         VARCHAR(100)  NOT NULL,
 offer_id              VARCHAR(100),
 customer_reference_id VARCHAR(100),
 status                VARCHAR(50),
 created_timestamp     TIMESTAMP     NOT NULL,
 updated_timestamp     TIMESTAMP,
 expiration_timestamp  TIMESTAMP
);
```

---

## Table: offer_snapshot

Captures offer details retrieved from Offer Management at intake time.
Includes customer_reference_id as it is part of the offer response.
Customer details retrieved from Customer Profile are not persisted here.
Linked to invitation_session. Retained for 12 months.

```sql
CREATE TABLE offer_snapshot
(
 snapshot_id           UUID           PRIMARY KEY,
 session_id            UUID           NOT NULL,
 offer_id              VARCHAR(100),
 customer_reference_id VARCHAR(100),
 loan_amount           DECIMAL(12,2),
 apr                   DECIMAL(5,2),
 term_months           INTEGER,
 expiration_date       DATE,
 retrieved_timestamp   TIMESTAMP,

 FOREIGN KEY (session_id)
  REFERENCES invitation_session (session_id)
);
```

---

## Table: application_intake_context

Records how an application journey was initiated. Channel-agnostic.
Linked to invitation_session for ITA; no session for DIRECT.

```sql
CREATE TABLE application_intake_context
(
 intake_id             UUID          PRIMARY KEY,
 application_source    VARCHAR(50)   NOT NULL,
 invitation_id         VARCHAR(100),
 session_id            UUID,
 offer_id              VARCHAR(100),
 customer_reference_id VARCHAR(100),
 prefill_status        VARCHAR(50),
 created_timestamp     TIMESTAMP     NOT NULL,

 FOREIGN KEY (session_id)
  REFERENCES invitation_session (session_id)
);
```

---

# Application Tables

## Table: application

Core application record. Created after SSN verification passes.
intake_id is nullable — present for INVITATION source, null for DIRECT.

```sql
CREATE TABLE application
(
 application_id     UUID          PRIMARY KEY,
 intake_id          UUID          NULL,
 application_source VARCHAR(50)   NOT NULL,
 application_status VARCHAR(50)   NOT NULL,
 created_timestamp  TIMESTAMP     NOT NULL,
 updated_timestamp  TIMESTAMP,

 FOREIGN KEY (intake_id)
  REFERENCES application_intake_context (intake_id)
);
```

---

## Table: applicant

Applicant personal and contact information as entered by the prospect.

SSN is **not stored in this table**. The raw SSN is verified via the SSN Verification Service
and then immediately tokenised via the Bolt Tokenisation Platform. Only the opaque `ssn_token`
(a Bolt reference string) is persisted. The raw SSN is discarded after tokenisation and is
never written to the database.

SSN is never logged or returned in any API response.

`citizenship` and `employment_status` are stored as VARCHAR to avoid coupling the schema to
enum ordinal values. Values match the `Citizenship` and `EmploymentStatus` domain enums.

`employer_name` and `employment_status` are nullable — not required when the applicant is RETIRED.

`annual_income` minimum enforced at the API layer (`@DecimalMin("0.00")`).

```sql
CREATE TABLE applicant
(
 applicant_id        UUID           PRIMARY KEY,
 application_id      UUID           NOT NULL,
 first_name          VARCHAR(100)   NOT NULL,
 last_name           VARCHAR(100)   NOT NULL,
 date_of_birth       DATE           NOT NULL,
 citizenship         VARCHAR(50)    NOT NULL,   -- US_CITIZEN | PERMANENT_RESIDENT | DACA | OTHER
 ssn_token           VARCHAR(255)   NOT NULL,   -- Bolt tokenisation reference; never raw SSN
 email               VARCHAR(200)   NOT NULL,
 phone               VARCHAR(20)    NOT NULL,
 street              VARCHAR(200)   NOT NULL,
 city                VARCHAR(100)   NOT NULL,
 state               VARCHAR(2)     NOT NULL,   -- 2-character US state code
 zip                 VARCHAR(10)    NOT NULL,
 employer_name       VARCHAR(200),              -- nullable; not required for RETIRED
 employment_status   VARCHAR(50),               -- EMPLOYED | SELF_EMPLOYED | RETIRED | OTHER
 annual_income       DECIMAL(12,2),             -- minimum 0.00; validated at API layer
 created_timestamp   TIMESTAMP      NOT NULL,
 updated_timestamp   TIMESTAMP,

 FOREIGN KEY (application_id)
  REFERENCES application (application_id)
);
```

---

## Table: loan_request

Requested loan details provided by the applicant.

```sql
CREATE TABLE loan_request
(
 loan_request_id    UUID           PRIMARY KEY,
 application_id     UUID           NOT NULL,
 requested_amount   DECIMAL(12,2)  NOT NULL,
 term_months        INTEGER        NOT NULL,
 loan_purpose       VARCHAR(100),
 created_timestamp  TIMESTAMP      NOT NULL,
 updated_timestamp  TIMESTAMP,

 FOREIGN KEY (application_id)
  REFERENCES application (application_id)
);
```

---

## Table: application_offer

Offer terms snapshotted at application creation time. These are the binding
loan terms associated with the application. Persisted independently of the
intake offer_snapshot to support 7-year audit retention.

Includes customer_reference_id as it is part of the offer data returned by
Offer Management. Customer details (name, address, contact) retrieved from
Customer Profile for prefill are NOT persisted here — those are PII used
only to populate the application form and are captured in the applicant table
as entered and confirmed by the applicant.

Populated for INVITATION source only. Null for DIRECT applications.

```sql
CREATE TABLE application_offer
(
 application_offer_id  UUID           PRIMARY KEY,
 application_id        UUID           NOT NULL,
 offer_id              VARCHAR(100)   NOT NULL,
 customer_reference_id VARCHAR(100)   NOT NULL,
 loan_amount           DECIMAL(12,2)  NOT NULL,
 apr                   DECIMAL(5,2)   NOT NULL,
 term_months           INTEGER        NOT NULL,
 expiration_date       DATE,
 captured_timestamp    TIMESTAMP      NOT NULL,

 FOREIGN KEY (application_id)
  REFERENCES application (application_id)
);
```

---

# Audit Table

## Table: application_audit

Immutable event log for application lifecycle. Linked to application.

```sql
CREATE TABLE application_audit
(
 audit_id          UUID          PRIMARY KEY,
 application_id    UUID,
 intake_id         UUID,
 event_type        VARCHAR(100)  NOT NULL,
 event_timestamp   TIMESTAMP     NOT NULL,
 payload           JSONB
);
```

---

# Indexes

## invitation_session

```
IDX_INVITATION_SESSION_INVITATION_ID
IDX_INVITATION_SESSION_STATUS
```

## application_intake_context

```
IDX_INTAKE_CONTEXT_INVITATION_ID
IDX_INTAKE_CONTEXT_SOURCE
IDX_INTAKE_CONTEXT_SESSION_ID
```

## application

```
IDX_APPLICATION_INTAKE_ID
IDX_APPLICATION_STATUS
IDX_APPLICATION_SOURCE
```

## applicant

```
IDX_APPLICANT_APPLICATION_ID
```

## application_audit

```
IDX_AUDIT_APPLICATION_ID
IDX_AUDIT_EVENT_TYPE
IDX_AUDIT_EVENT_TIMESTAMP
```

---

# Data Retention

| Table                       | Retention     | Reason                        |
| --------------------------- | ------------- | ----------------------------- |
| invitation_session          | 12 months     | Intake process record         |
| offer_snapshot              | 12 months     | Intake offer record           |
| application_intake_context  | 7 years       | Linked to application audit   |
| application                 | 7 years       | Regulatory requirement        |
| applicant                   | 7 years       | Regulatory requirement        |
| loan_request                | 7 years       | Regulatory requirement        |
| application_offer           | 7 years       | Binding loan terms audit      |
| application_audit           | 7 years       | Regulatory requirement        |

Adjust based on applicable regulatory requirements.
