# Offer Acceptance Capability Specification

# Personal Loan Acquisition Platform

Version: 1.0

Status: Active

Capability: Offer Acceptance

Owner: Acquisition Platform

---

# 1. Purpose

The Offer Acceptance capability owns the post-approval e-sign and declaration capture flow.

When an application receives an APPROVED final decision, this capability creates an acceptance session, presents the applicable declarations to the applicant, captures the e-sign, and publishes the `ESignCompleted` event that drives the application into `OFFER_ACCEPTED` state.

---

# 2. Business Objective

The capability shall:

* Initialise an offer acceptance session when an approval is received.
* Expose declarations for applicant review.
* Capture the applicant's electronic signature across all mandatory declarations.
* Publish an `ESignCompleted` event after successful e-sign capture.
* Ensure idempotency — duplicate approval events must not create duplicate sessions.

---

# 3. Scope

## In Scope

Offer acceptance session creation on `FinalDecisionApproved` event.

Declaration retrieval API.

E-sign submission and validation.

`ESignCompleted` event publication.

Idempotency on duplicate events.

---

## Out Of Scope

Offer generation and pricing.

Credit decisioning.

Document collection.

Application state ownership (state transitions are owned by application-management-service).

Funding execution.

---

# 4. Workflow

```
FinalDecisionApproved event received
  → Create OfferAcceptanceSession (status: PENDING_ESIGN)
  → Applicant retrieves declarations: GET /applications/{id}/declarations
  → Applicant reviews and accepts all mandatory declarations
  → POST /applications/{id}/esign
  → Record ESignRecord (applicationId, signedAt, ipAddress, declarationsAccepted)
  → Update OfferAcceptanceSession status to ESIGNED
  → Publish ESignCompleted event
  → application-management-service transitions application to OFFER_ACCEPTED
```

---

# 5. Domain Model

## OfferAcceptanceSession

```
sessionId            UUID
applicationId        UUID
status               PENDING_ESIGN | ESIGNED
declarations         List<Declaration>
createdTimestamp     ISO-8601
```

## Declaration

```
declarationId        UUID
declarationType      String (e.g. TERMS_AND_CONDITIONS, CREDIT_CONSENT)
title                String
content              String
mandatory            Boolean
```

## ESignRecord

```
eSignId              UUID
applicationId        UUID
signedAt             ISO-8601
ipAddress            String
declarationsAccepted List<UUID>   (declarationId values)
```

---

# 6. Functional Requirements

## FR-001 Session Initialisation on Approval

- **WHEN** a `FinalDecisionApproved` event is received
- **THEN** an `OfferAcceptanceSession` SHALL be created for the application with status `PENDING_ESIGN`
- **THEN** declarations applicable to the loan product SHALL be attached to the session

### Idempotency

- **WHEN** a `FinalDecisionApproved` event is received for an application that already has a session
- **THEN** the system SHALL NOT create a duplicate session
- **THEN** the event SHALL be logged and discarded

---

## FR-002 Declarations Retrieval

`GET /applications/{applicationId}/declarations`

Response:

```json
{
  "applicationId": "APP123",
  "declarations": [
    {
      "declarationId": "decl-001",
      "declarationType": "TERMS_AND_CONDITIONS",
      "title": "Terms and Conditions",
      "content": "...",
      "mandatory": true
    },
    {
      "declarationId": "decl-002",
      "declarationType": "CREDIT_CONSENT",
      "title": "Credit Consent",
      "content": "...",
      "mandatory": true
    }
  ]
}
```

### Error Codes

| Code | HTTP | Condition |
|------|------|-----------|
| `ACCEPTANCE_SESSION_NOT_FOUND` | 404 | No session exists for this application |

---

## FR-003 E-Sign Submission

`POST /applications/{applicationId}/esign`

Request:

```json
{
  "declarationsAccepted": ["decl-001", "decl-002"],
  "ipAddress": "203.0.113.45"
}
```

Response: `200 OK` on success.

### Validation

All mandatory `declarationId` values must be present in `declarationsAccepted`.

### Business Rules

- If any mandatory declaration is missing, the request is rejected — no partial acceptance.
- If the session is already `ESIGNED`, the request is rejected — no re-sign.

### Error Codes

| Code | HTTP | Condition |
|------|------|-----------|
| `MANDATORY_DECLARATION_NOT_ACCEPTED` | 422 | One or more mandatory declarations absent |
| `ESIGN_ALREADY_COMPLETED` | 409 | Session is already ESIGNED |

---

# 7. Event Published — ESignCompleted

Published to the platform event bus after successful e-sign capture.

Topic: `offer-acceptance.esign-completed`

```json
{
  "applicationId": "APP123",
  "signedAt": "2026-06-30T10:15:00Z",
  "correlationId": "corr-abc-123"
}
```

Consumed by: `application-management-service` — transitions application to `OFFER_ACCEPTED`.

---

# 8. Event Consumed — FinalDecisionApproved

Published by: `pricing-orchestration-service`

Topic: `pricing.final-decision-approved`

Action: Create `OfferAcceptanceSession` for the application.

---

# 9. API Summary

| Method | Path | Purpose |
|--------|------|---------|
| `GET` | `/applications/{id}/declarations` | Retrieve declarations for applicant review |
| `POST` | `/applications/{id}/esign` | Submit e-sign with accepted declarations |

All endpoints require `Authorization: Bearer <token>`.

---

# 10. Acceptance Criteria

The capability is complete when:

- An `OfferAcceptanceSession` is created when a `FinalDecisionApproved` event is received.
- Duplicate `FinalDecisionApproved` events do not create duplicate sessions.
- Declarations are returned with all required fields.
- E-sign is accepted only when all mandatory declarations are included.
- E-sign is rejected with `MANDATORY_DECLARATION_NOT_ACCEPTED` when mandatory declarations are missing.
- E-sign is rejected with `ESIGN_ALREADY_COMPLETED` when the session is already signed.
- `ESignCompleted` is published after successful e-sign.
- `application-management-service` transitions the application to `OFFER_ACCEPTED` on `ESignCompleted`.

---

# 11. Related Documents

```
docs/architecture/002-application-state-machine.md
docs/architecture/005-event-driven-architecture.md
openspec/changes/post-decision-flow/specs/offer-acceptance/spec.md
openspec/application-management/spec.md
```
