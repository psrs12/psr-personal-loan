# Pricing Orchestration Capability Specification

# Personal Loan Acquisition Platform

Version: 1.0

Status: Active

Capability: Pricing Orchestration

Owner: Acquisition Platform

---

# 1. Purpose

The Pricing Orchestration capability coordinates the credit evaluation and decision workflow for a personal loan application.

It orchestrates the soft pull, offer presentation and selection, hard pull, and final decision routing. It does not own credit policy, offer pricing logic, or decisioning rules — those belong to enterprise platforms. The platform's role is to sequence the workflow, translate events, route outcomes, and publish domain events to downstream capabilities.

---

# 2. Business Objective

The capability shall:

* Initiate a soft credit pull when an application is submitted.
* Retrieve and present priced offers based on soft pull results.
* Capture the applicant's offer selection and hard pull consent.
* Initiate a hard credit pull after offer confirmation.
* Submit a final decision request after the hard pull completes.
* Route the application based on the final decision outcome.
* Publish domain events for each routing outcome.

---

# 3. Scope

## In Scope

Soft pull initiation and result consumption.

Offer retrieval and presentation.

Offer selection capture.

Hard pull consent capture.

Hard pull initiation and result consumption.

Final decision submission.

Outcome routing: APPROVED, DECLINED, REFERRED, DOCUMENTS_REQUIRED.

Domain event publication for all outcomes.

---

## Out Of Scope

Credit scoring and credit policy.

Offer generation and pricing algorithms.

Decision rules and decisioning logic.

Document collection.

Offer acceptance and e-sign.

Funding execution.

---

# 4. Workflow

```
ApplicationSubmitted
  → Initiate Soft Pull (Credit Management Platform)
  → SoftPullCompleted
  → Retrieve priced offers (Offer Management Platform)
  → Present offers to applicant via pricing-offers-ui
  → Applicant selects offer and consents to hard pull
  → OfferConfirmed event (UI → pricing-orchestration-service)
  → Initiate Hard Pull (Credit Management Platform)
  → HardPullCompleted
  → Submit Final Decision Request (Decision Platform)
  → Route on outcome:
      APPROVED            → publish FinalDecisionApproved
      DECLINED            → publish FinalDecisionDeclined
      REFERRED            → publish FinalDecisionReferred
      DOCUMENTS_REQUIRED  → publish FinalDecisionDocumentsRequired
```

---

# 5. Functional Requirements

## FR-001 Soft Pull Initiation

The system SHALL initiate a soft credit pull when an `ApplicationSubmitted` event is received.

Request to Credit Management Platform includes: `applicationId`, `applicantSsnToken`, `dateOfBirth`.

---

## FR-002 Offer Retrieval

The system SHALL retrieve priced offers from the Offer Management Platform after soft pull completion.

Offer attributes returned: `offerId`, `loanAmount`, `term`, `apr`, `monthlyPayment`, `expiresAt`.

---

## FR-003 Offer Selection

The system SHALL accept the applicant's offer selection and record the `selectedOfferId`.

The applicant must consent to a hard pull before selection is confirmed.

---

## FR-004 Hard Pull Initiation

The system SHALL initiate a hard credit pull after offer confirmation.

Request to Credit Management Platform includes: `applicationId`, `applicantSsnToken`, `dateOfBirth`, `selectedOfferId`.

---

## FR-005 Final Decision Submission

The system SHALL submit a final decision request to the Decision Platform after `HardPullCompleted`.

Request includes: `applicationId`, `selectedOfferId`, `hardPullCreditReportReferenceId`.

---

## FR-006 Outcome Routing

The system SHALL route the application based on the final decision outcome.

### Outcome: APPROVED

- Transition application state to `APPROVED` via `PATCH /applications/{id}/status`.
- Confirm selected offer.
- Publish `FinalDecisionApproved`.

### Outcome: DECLINED

- Transition application state to `DECLINED`.
- Publish `FinalDecisionDeclined` with reason code.
- Trigger adverse action notification workflow.

### Outcome: REFERRED

- Transition application state to `REFERRED`.
- Retain selected offer without modification.
- Do not initiate a new hard pull.
- Publish `FinalDecisionReferred`.

### Outcome: DOCUMENTS_REQUIRED

- Transition application state to `DOCUMENTS_REQUIRED`.
- Retain selected offer without modification.
- Publish `FinalDecisionDocumentsRequired` containing `applicationId` and the verbatim list of Decision Engine document type codes.
- The service SHALL NOT map or interpret document type codes — it forwards them as received.

---

# 6. Domain Events

## FinalDecisionApproved

```json
{
  "applicationId": "APP123",
  "selectedOfferId": "OFF456",
  "correlationId": "corr-abc-123",
  "timestamp": "2026-06-30T10:00:00Z"
}
```

## FinalDecisionDeclined

```json
{
  "applicationId": "APP123",
  "reasonCode": "CREDIT_RISK",
  "correlationId": "corr-abc-123",
  "timestamp": "2026-06-30T10:00:00Z"
}
```

## FinalDecisionReferred

```json
{
  "applicationId": "APP123",
  "correlationId": "corr-abc-123",
  "timestamp": "2026-06-30T10:00:00Z"
}
```

## FinalDecisionDocumentsRequired

```json
{
  "applicationId": "APP123",
  "correlationId": "corr-abc-123",
  "timestamp": "2026-06-30T10:00:00Z",
  "documents": [
    { "decisionEngineCode": "BANK_STMT_3M", "count": 1 },
    { "decisionEngineCode": "PAYSLIP_2", "count": 1 }
  ]
}
```

The `documents` array contains Decision Engine codes verbatim. The ACL mapping to platform `DocumentType` values is the responsibility of `document-service`.

---

# 7. API

## Offer Selection

`POST /applications/{applicationId}/offers/confirm`

Request:

```json
{
  "selectedOfferId": "OFF456",
  "hardPullConsent": true,
  "consentTimestamp": "2026-06-30T09:55:00Z"
}
```

Response: `200 OK` with confirmed offer summary.

---

# 8. UI Component

The `<pricing-offer-selector>` web component (served by `pricing-offers-ui`) handles:

- Display of retrieved offers.
- Offer selection by applicant.
- Hard pull consent capture.
- Firing the `offer-confirmed` custom event consumed by the micro-frontend shell.

---

# 9. External Integrations

| System | Pattern | Operations |
|--------|---------|-----------|
| Credit Management Platform | Synchronous REST | `initiateSoftPull()`, `initiateHardPull()` |
| Offer Management Platform | Synchronous REST | `getPricedOffers(softPullRef)` |
| Decision Platform | Synchronous REST | `submitFinalDecision(applicationId, offerId, creditReportRef)` |

All integrations are accessed through port interfaces and infrastructure adapters. No direct calls from controllers or domain objects.

---

# 10. Business Rules

- Hard pull must not be initiated without explicit applicant consent.
- Selected offer must be retained unchanged during REFERRED and DOCUMENTS_REQUIRED routing.
- No new hard pull shall be initiated during manual underwriting (REFERRED).
- Document type codes in `FinalDecisionDocumentsRequired` must be forwarded verbatim — no mapping in this service.

---

# 11. Acceptance Criteria

The capability is complete when:

- Soft pull is initiated on application submission.
- Offers are retrieved and presented.
- Offer selection with hard pull consent is captured.
- Hard pull is initiated after offer confirmation.
- Final decision is submitted after hard pull completion.
- APPROVED, DECLINED, REFERRED, and DOCUMENTS_REQUIRED outcomes are routed correctly.
- Correct domain event is published for each outcome.
- `FinalDecisionDocumentsRequired` carries verbatim Decision Engine codes.
- Application state is updated via `application-management-service` for each outcome.

---

# 12. Related Documents

```
docs/architecture/005-event-driven-architecture.md
docs/architecture/006-integration-patterns.md
openspec/changes/post-decision-flow/specs/pricing-orchestration/spec.md
```
