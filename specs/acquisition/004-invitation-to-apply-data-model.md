# acquisition/invitation-to-apply/004-invitation-to-apply-data-model.md

# Personal Loan Acquisition Platform

# Invitation To Apply Data Model Specification

Version: 1.0

Status: Draft

Owner: Acquisition Domain

---

# 1. Purpose

This document defines the data model for the Invitation To Apply capability.

The model supports:

* Invitation validation
* Offer association tracking
* Customer prefilling
* Application initiation tracking
* Auditability
* Operational support

---

# 2. Data Ownership

## ITA Service Owns

The ITA service owns:

* Invitation usage tracking
* Invitation lifecycle status
* Offer reference
* Application association
* Validation history

---

## External Systems Own

### Offer Management

Owns:

* Offer details
* Offer pricing
* Eligibility
* Customer offer profile

---

### Application Service

Owns:

* Application details
* Application state
* Applicant lifecycle

---

# 3. Database

Database:

```text
invitation_to_apply_db
```

---

# 4. Entity Relationship Overview

```text
Invitation

    |

    |

Offer Reference


Invitation

    |

    |

Application Reference


Invitation

    |

    |

Validation History
```

---

# 5. Main Entities

## 5.1 Invitation

Purpose:

Stores invitation lifecycle information.

---

Table:

```sql
invitation
```

---

Structure:

| Column                 | Type      | Description                   |
| ---------------------- | --------- | ----------------------------- |
| invitation_id          | UUID      | Internal identifier           |
| external_invitation_id | VARCHAR   | Marketing invitation ID       |
| offer_id               | VARCHAR   | Offer Management reference    |
| status                 | VARCHAR   | Invitation status             |
| application_id         | UUID      | Created application reference |
| expires_at             | TIMESTAMP | Invitation expiry             |
| created_at             | TIMESTAMP | Created time                  |
| updated_at             | TIMESTAMP | Updated time                  |
| created_by             | VARCHAR   | Creator                       |
| updated_by             | VARCHAR   | Modifier                      |
| version                | INTEGER   | Optimistic locking            |

---

# 6. Invitation Status

Allowed values:

```text
CREATED

VALIDATING

VALIDATED

OFFER_RETRIEVED

READY_TO_APPLY

APPLICATION_CREATED

INVALID

EXPIRED

REJECTED
```

---

# 7. SQL Definition

Example:

```sql
CREATE TABLE invitation
(
 invitation_id UUID PRIMARY KEY,

 external_invitation_id VARCHAR(100) NOT NULL UNIQUE,

 offer_id VARCHAR(100),

 application_id UUID,

 status VARCHAR(30) NOT NULL,

 expires_at TIMESTAMP,

 created_at TIMESTAMP NOT NULL,

 updated_at TIMESTAMP,

 created_by VARCHAR(100),

 updated_by VARCHAR(100),

 version INTEGER DEFAULT 0
);
```

---

# 8. Indexes

Required indexes:

```sql
CREATE INDEX idx_invitation_external_id
ON invitation(external_invitation_id);
```

---

```sql
CREATE INDEX idx_invitation_status
ON invitation(status);
```

---

```sql
CREATE INDEX idx_invitation_offer
ON invitation(offer_id);
```

---

# 9. Invitation Usage Rules

## Rule 1

Invitation ID must be unique.

---

## Rule 2

One invitation maps to one application.

---

Constraint:

```text
external_invitation_id

UNIQUE
```

---

# 10. Validation History Entity

Purpose:

Track every invitation validation attempt.

---

Table:

```sql
invitation_validation_history
```

---

Structure:

| Column            | Type      |
| ----------------- | --------- |
| validation_id     | UUID      |
| invitation_id     | UUID      |
| validation_status | VARCHAR   |
| validation_reason | VARCHAR   |
| correlation_id    | VARCHAR   |
| validated_at      | TIMESTAMP |

---

# 11. SQL Definition

```sql
CREATE TABLE invitation_validation_history
(
 validation_id UUID PRIMARY KEY,

 invitation_id UUID NOT NULL,

 validation_status VARCHAR(30),

 validation_reason VARCHAR(255),

 correlation_id VARCHAR(100),

 validated_at TIMESTAMP
);
```

---

# 12. Validation Status

Values:

```text
SUCCESS

FAILED

EXPIRED

DUPLICATE

ERROR
```

---

# 13. Applicant Prefill Snapshot

Purpose:

Store customer data returned from Offer Management during invitation flow.

This is NOT the customer master.

---

Table:

```sql
invitation_customer_snapshot
```

---

Structure:

| Column        | Type      |
| ------------- | --------- |
| snapshot_id   | UUID      |
| invitation_id | UUID      |
| first_name    | VARCHAR   |
| last_name     | VARCHAR   |
| address_line1 | VARCHAR   |
| address_line2 | VARCHAR   |
| city          | VARCHAR   |
| state         | VARCHAR   |
| postal_code   | VARCHAR   |
| captured_at   | TIMESTAMP |

---

# 14. PII Handling

Customer snapshot contains PII.

Requirements:

* Encryption at rest
* Restricted access
* Masking in logs

---

Protected fields:

```text
first_name

last_name

address
```

---

# 15. Audit Entity

Purpose:

Track business events.

---

Table:

```sql
invitation_audit
```

---

Structure:

| Column         | Type      |
| -------------- | --------- |
| audit_id       | UUID      |
| invitation_id  | UUID      |
| event_type     | VARCHAR   |
| event_time     | TIMESTAMP |
| correlation_id | VARCHAR   |
| actor          | VARCHAR   |

---

# 16. Audit Events

Examples:

```text
InvitationValidated

OfferRetrieved

InvitationExpired

ApplicationCreated
```

---

# 17. JPA Entity Model

Example:

```java
@Entity
class Invitation {

 @Id
 UUID invitationId;

 String externalInvitationId;

 String offerId;

 InvitationStatus status;

 UUID applicationId;

 Instant expiresAt;

}
```

---

# 18. Enum Design

```java
public enum InvitationStatus {

 CREATED,

 VALIDATING,

 VALIDATED,

 OFFER_RETRIEVED,

 READY_TO_APPLY,

 APPLICATION_CREATED,

 INVALID,

 EXPIRED,

 REJECTED
}
```

---

# 19. Repository Model

```java
interface InvitationRepository
extends JpaRepository<Invitation, UUID>
{

 Optional<Invitation>
 findByExternalInvitationId(String id);

}
```

---

# 20. Transaction Boundaries

Invitation creation:

Transactional

---

Validation update:

Transactional

---

Application creation reference:

Transactional update

---

# 21. Data Retention

Invitation records:

Retained according to enterprise retention policy.

---

Audit history:

Longer retention required.

---

# 22. Data Migration

All changes must use:

Database migration scripts.

Example:

```text
V1_create_invitation.sql

V2_add_validation_history.sql

V3_add_snapshot.sql
```

---

# 23. Performance Considerations

Optimize:

* Invitation lookup
* Status search
* Application association

---

Expected query:

```sql
find invitation by external_invitation_id
```

must be indexed.

---

# 24. Security Controls

Required:

* Encryption
* Database access control
* Audit access
* Least privilege

---

# 25. Data Model Acceptance Criteria

☐ Invitation lifecycle stored

☐ Offer reference maintained

☐ Application linkage supported

☐ Validation history available

☐ Audit history available

☐ PII protected

☐ No external system ownership violation

---

# Related Documents

* 001-invitation-to-apply-spec.md
* 002-invitation-to-apply-state-machine.md
* 003-invitation-to-apply-api-spec.md
* 002-database-design-guidelines.md
* 003-error-handling-standard.md
