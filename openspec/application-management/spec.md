# Application Management Capability Specification

# Personal Loan Acquisition Platform

Version: 1.0

Status: Active

Capability: Application Management

Owner: Acquisition Platform

---

# 1. Purpose

The Application Management capability manages the complete personal loan application lifecycle from channel intake through submission, post-decision state progression, and applicant re-entry.

The capability provides a unified application experience regardless of acquisition channel and is the sole owner of application state within the platform.

---

# 2. Business Objective

The capability shall:

* Accept applications from multiple intake channels (Invitation To Apply, Direct).
* Validate invitation tokens and prefill applicant data from enterprise platforms.
* Own and enforce the application state machine.
* Track all state transitions with a full audit trail.
* Support applicant re-entry to an in-progress journey via a secure login flow.
* Publish domain events on all significant lifecycle transitions.

---

# 3. Scope

## In Scope

Invitation token validation.

Offer and customer information retrieval.

Application intake context creation.

Application creation and prefill.

Applicant information management.

Application save and resume.

Application submission.

Application state machine ownership and enforcement.

Applicant re-entry login.

Application audit event generation.

---

## Out Of Scope

Offer generation and pricing.

Identity verification execution.

Fraud evaluation.

Credit evaluation.

Decision execution.

Document collection.

Offer acceptance and e-sign.

Funding execution.

Loan servicing.

---

# 4. Sub-Capabilities

## 4.1 Application Intake — Invitation To Apply

### Purpose

Enable a prospect who received a marketing invitation to initiate an application with prefilled data.

### Flow

```
Prospect enters invitation identifier
  → Validate invitation with Offer Management Platform
  → Retrieve offer details (offerId, amount, term, APR)
  → Retrieve customer reference identifier
  → Retrieve customer profile (name, address)
  → Create ApplicationIntakeContext (source: INVITATION)
  → Create Application with prefilled fields
  → Applicant completes editable fields
```

### Business Rules

- Invitation must exist, be active, and not expired.
- Offer must be available at the time of intake.
- Name and address prefilled from the invitation are read-only — the applicant cannot edit them.
- Only non-identity fields (employment, income, financial obligations) are editable.
- Application creation must not depend on invitation availability — failures are handled gracefully.

### Error Codes

| Code | HTTP | Condition |
|------|------|-----------|
| `INVITATION_NOT_FOUND` | 404 | Invitation identifier does not exist |
| `INVITATION_EXPIRED` | 422 | Invitation has passed its expiry date |
| `OFFER_UNAVAILABLE` | 422 | Offer Management Platform cannot return offer |
| `CUSTOMER_INFORMATION_UNAVAILABLE` | 422 | Customer Profile Platform cannot return profile |

---

## 4.2 Application Intake — Direct

### Purpose

Enable a prospect to start an application without an invitation.

### Flow

```
Prospect initiates direct application
  → Create ApplicationIntakeContext (source: DIRECT)
  → Create empty Application
  → Applicant provides all information
```

---

## 4.3 Application Lifecycle Management

### Purpose

Own, enforce, and audit every application state transition.

### State Machine

```
CREATED
  → STARTED
  → IN_PROGRESS
  → SUBMITTED
  → PROCESSING
  → APPROVED | DECLINED | REFERRED | DOCUMENTS_REQUIRED
  → UNDERWRITING             (from REFERRED or DOCUMENTS_REQUIRED after DocumentsCompleted)
  → OFFER_ACCEPTED           (from APPROVED after ESignCompleted)
  → FUNDING_PENDING          (from OFFER_ACCEPTED)
  → FUNDED
  → COMPLETED
```

Terminal states: `DECLINED`, `CANCELLED`, `EXPIRED`, `COMPLETED`

### State Transition Rules

| Current State | Allowed Next States | Trigger |
|---------------|--------------------|---------:|
| `CREATED` | `STARTED` | Application started by applicant |
| `STARTED` | `IN_PROGRESS` | First save |
| `IN_PROGRESS` | `SUBMITTED` | Application submitted |
| `SUBMITTED` | `PROCESSING` | Downstream processing begins |
| `PROCESSING` | `APPROVED`, `DECLINED`, `REFERRED`, `DOCUMENTS_REQUIRED` | Final decision received |
| `DOCUMENTS_REQUIRED` | `UNDERWRITING` | `DocumentsCompleted` event received |
| `APPROVED` | `OFFER_ACCEPTED` | `ESignCompleted` event received |
| `REFERRED` | `UNDERWRITING` | Underwriter assignment |
| `UNDERWRITING` | `APPROVED`, `DECLINED` | Underwriter decision |
| `OFFER_ACCEPTED` | `FUNDING_PENDING` | Funding initiated |
| `FUNDING_PENDING` | `FUNDED` | Funding confirmed |
| `FUNDED` | `COMPLETED` | Post-funding completion |

Any transition not listed above SHALL be rejected with `InvalidStateTransitionException`.

### Audit Trail

Every state transition SHALL be recorded in `application_audit` with:
- `previousState`
- `newState`
- `timestamp`
- `actor` (system or user)
- `correlationId`

---

## 4.4 Applicant Login

### Purpose

Provide applicants with secure re-entry to an in-progress application journey.

### Endpoint

`POST /applications/login` — public endpoint, no `Authorization` header required.

### Request

```json
{
  "applicationId": "APP123",
  "last4SSN": "1234",
  "dateOfBirth": "1985-06-15"
}
```

### Response

```json
{
  "sessionToken": "<JWT>",
  "expiresAt": "2026-06-30T11:30:00Z",
  "applicationId": "APP123",
  "applicationStatus": "DOCUMENTS_REQUIRED"
}
```

Session token: JJWT, 30-minute expiry.

### Business Rules

- Login is rejected for applications in terminal states (DECLINED, FUNDED, COMPLETED, CANCELLED, EXPIRED).
- Verification is delegated to `VerificationPort`.
- The `StubVerificationAdapter` is active when `verification.stub.enabled=true`.
- The stub validates `last4SSN` and `dateOfBirth` against data held in the application record.
- The stub is replaceable by a dedicated verification-service adapter without domain changes.
- `applicationStatus` is included in the response so the UI shell can route immediately without a second API call.

### Error Codes

| Code | HTTP | Condition |
|------|------|-----------|
| `APPLICATION_NOT_FOUND` | 404 | No application with the given ID |
| `APPLICANT_VERIFICATION_FAILED` | 401 | Credentials do not match |
| `APPLICATION_NOT_ACCESSIBLE` | 422 | Application is in a terminal state |
| `VERIFICATION_SERVICE_UNAVAILABLE` | 503 | Stub disabled and real adapter not wired |

---

# 5. Domain Model

## Application Aggregate

```
applicationId          UUID
intakeId               UUID
applicationSource      INVITATION | DIRECT | PARTNER
applicationStatus      (see state machine above)
applicant              Applicant entity
loanRequest            LoanRequest entity
createdTimestamp       ISO-8601
updatedTimestamp       ISO-8601
```

## Applicant Entity

```
applicantId
firstName
lastName
dateOfBirth
ssn                    (token — never stored in plaintext)
contactInformation
address
employmentInformation
```

## LoanRequest Entity

```
requestedAmount
loanPurpose
term
```

## ApplicationIntakeContext

```
intakeId
applicationSource
invitationId
offerId
customerReferenceId
prefillStatus
createdTimestamp
```

---

# 6. Events Published

| Event | Trigger |
|-------|---------|
| `ApplicationCreated` | Application created |
| `ApplicationSubmitted` | Application submitted |
| `APPLICATION_STATE_CHANGED` | Any state transition |

### APPLICATION_STATE_CHANGED Payload

```json
{
  "applicationId": "APP123",
  "previousState": "APPROVED",
  "newState": "OFFER_ACCEPTED",
  "timestamp": "2026-06-30T10:15:00Z",
  "actor": "SYSTEM",
  "correlationId": "corr-abc-123"
}
```

---

# 7. Events Consumed

| Event | Action |
|-------|--------|
| `ESignCompleted` | Transition application to `OFFER_ACCEPTED` |
| `DocumentsCompleted` | Transition application to `UNDERWRITING` |

---

# 8. API Summary

| Method | Path | Purpose |
|--------|------|---------|
| `POST` | `/api/v1/applications` | Create application |
| `GET` | `/api/v1/applications/{id}` | Retrieve application |
| `PUT` | `/api/v1/applications/{id}` | Update application |
| `POST` | `/api/v1/applications/{id}/submit` | Submit application |
| `PATCH` | `/api/v1/applications/{id}/status` | Update application status (internal) |
| `POST` | `/applications/login` | Applicant re-entry login (public) |

---

# 9. Security Rules

- SSN is stored as a token — never in plaintext.
- SSN must not appear in any log output.
- All endpoints except `POST /applications/login` require `Authorization: Bearer <token>`.
- Session tokens expire after 30 minutes.

---

# 10. Persistence

Database: PostgreSQL

Migration: Flyway

Tables: `application`, `applicant`, `loan_request`, `application_intake_context`, `application_audit`

---

# 11. Acceptance Criteria

The capability is complete when:

- Application can be created from ITA intake with prefilled read-only identity fields.
- Application can be created without invitation.
- Application state machine enforces all valid transitions and rejects invalid ones.
- Every state transition is recorded in the audit trail.
- Applicant can log in with applicationId, last4SSN, and DOB and receive a session token.
- Login is rejected for terminal applications.
- `applicationStatus` is returned in the login response.
- All domain events are published on the Kafka topics.
- SSN is never logged or stored in plaintext.

---

# 12. Related Documents

```
docs/architecture/002-application-state-machine.md
docs/architecture/005-event-driven-architecture.md
docs/architecture/007-security-architecture.md
openspec/changes/post-decision-flow/specs/application-state-machine/spec.md
openspec/changes/post-decision-flow/specs/applicant-login/spec.md
services/application-management-service/openspec/specs/002-application-intake-spec.md
services/application-management-service/openspec/specs/003-application-spec.md
```
