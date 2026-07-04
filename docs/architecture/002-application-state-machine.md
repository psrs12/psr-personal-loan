# Personal Loan Acquisition Platform

## Application State Machine Specification

Version: 2.0
Status: Current
Owner: Personal Loan Acquisition Platform

---

# 1. Purpose

This document defines the complete application lifecycle states and valid state transitions for the Personal Loan Acquisition Platform.

All services that read or write application status must comply with this specification. The `application-management-service` is the sole authority for enforcing and persisting state.

---

# 2. Complete State Diagram

```
                              ┌──────────┐
                              │  CREATED │
                              └────┬─────┘
                                   │ applicant begins
                                   ▼
                              ┌──────────┐
                              │  STARTED │
                              └────┬─────┘
                                   │ data entered
                                   ▼
                           ┌─────────────┐
                           │ IN_PROGRESS │
                           └──────┬──────┘
                                  │ applicant submits
                                  ▼
                           ┌───────────┐
                           │ SUBMITTED │
                           └─────┬─────┘
                                 │ validation complete
                                 ▼
                          ┌────────────┐
                          │ PROCESSING │
                          └─────┬──────┘
                                │
               ┌────────────────┼──────────────────┐
               │                │                  │
               ▼                ▼                  ▼
         ┌──────────┐     ┌──────────┐      ┌──────────┐
         │ DECLINED │     │ APPROVED │      │ REFERRED │
         │(terminal)│     └────┬─────┘      │(manual UW│
         └──────────┘          │            └──────────┘
                               │
              ┌────────────────┤
              │                │
              ▼                │ (no docs needed)
 ┌─────────────────────┐       │
 │  DOCUMENTS_REQUIRED │       │
 └──────────┬──────────┘       │
            │ DocumentsCompleted event
            ▼                  │
     ┌────────────┐            │ ESignCompleted event
     │UNDERWRITING│            │
     └─────┬──────┘            │
           │ review complete   │
           ▼                   │
     ┌──────────┐              │
     │ APPROVED │◄─────────────┘
     └────┬─────┘
          │ ESignCompleted event
          ▼
   ┌───────────────┐
   │ OFFER_ACCEPTED│
   └──────┬────────┘
          │
          ▼
   ┌───────────────┐
   │FUNDING_PENDING│
   └──────┬────────┘
          │ funding complete
          ▼
      ┌────────┐
      │ FUNDED │
      └───┬────┘
          │
          ▼
     ┌──────────┐
     │ COMPLETED│ (terminal)
     └──────────┘
```

---

# 3. State Definitions

## CREATED
Application record exists in the system. Triggered by the ITA flow or direct application entry. Minimal data available at this point.

## STARTED
The applicant has entered the application journey. Application ID, applicant reference, and source channel are established.

## IN_PROGRESS
The applicant is actively completing their application. Data can be updated and progress saved for resumption.

## SUBMITTED
The applicant has completed and submitted all required application information. Enterprise evaluations are triggered.

## PROCESSING
The application is undergoing enterprise evaluations: identity verification, fraud check, soft credit pull, and decisioning.

## APPROVED
The Decision Engine returned an approval outcome. The application is ready for offer presentation and e-signature.

## DECLINED
The Decision Engine returned a decline outcome. **Terminal state.** An adverse action notice is issued.

## REFERRED
The Decision Engine referred the application for manual underwriting — no document list is associated. Human review determines the outcome.

## DOCUMENTS_REQUIRED
The Decision Engine requested specific documents before a final decision can be made. The document-service receives the document type codes and creates `DocumentRequirement` records for the applicant to fulfil.

## OFFER_ACCEPTED
The applicant has reviewed all declarations and completed the e-signature. Triggered by the `ESignCompleted` event from offer-acceptance-service.

## UNDERWRITING
All required documents have been submitted and verified. The application is under manual review. Triggered by the `DocumentsCompleted` event from document-service.

## FUNDING_PENDING
A funding request has been submitted to the Funding Platform. Awaiting disbursement confirmation.

## FUNDED
Loan funds have been disbursed successfully.

## COMPLETED
The full application lifecycle is complete. **Terminal state.**

## CANCELLED
The applicant or an agent cancelled the application. **Terminal state.**

## EXPIRED
The application exceeded its validity window without progression. **Terminal state.**

---

# 4. State Transition Table

| From | To | Trigger | Mechanism |
|------|----|---------|-----------|
| CREATED | STARTED | Applicant begins journey | Direct API call |
| STARTED | IN_PROGRESS | Applicant enters data | Direct API call |
| IN_PROGRESS | SUBMITTED | Applicant submits form | Direct API call |
| SUBMITTED | PROCESSING | Validation passes | Direct API call |
| PROCESSING | APPROVED | Decision Engine: approved | pricing-orchestration-service → REST |
| PROCESSING | DECLINED | Decision Engine: declined | pricing-orchestration-service → REST |
| PROCESSING | REFERRED | Decision Engine: manual review | pricing-orchestration-service → REST |
| PROCESSING | DOCUMENTS_REQUIRED | Decision Engine: docs required | document-service → REST (after Kafka event) |
| APPROVED | OFFER_ACCEPTED | ESignCompleted event | Kafka consumer in app-management-service |
| DOCUMENTS_REQUIRED | UNDERWRITING | DocumentsCompleted event | Kafka consumer in app-management-service |
| UNDERWRITING | APPROVED | Manual review complete | Direct API call |
| OFFER_ACCEPTED | FUNDING_PENDING | Funding request submitted | funding-request-service (planned) |
| FUNDING_PENDING | FUNDED | Funding confirmation received | funding-request-service (planned) |
| FUNDED | COMPLETED | Final completion | application-management-service |
| Any non-terminal | CANCELLED | Agent or applicant cancels | Direct API call |
| Any non-terminal | EXPIRED | TTL exceeded | Scheduled job |

---

# 5. Terminal States

The following states are terminal. No further transitions are permitted:

| State | Reason |
|-------|--------|
| DECLINED | Credit or fraud decision negative |
| CANCELLED | Explicit cancellation |
| EXPIRED | TTL exceeded |
| COMPLETED | Lifecycle complete |

Applicant login (via `POST /applications/login`) is rejected if the application is in a terminal state.

---

# 6. Invalid Transitions

The `ApplicationStateMachine` class enforces the valid transition map. Any attempt to transition outside the allowed set throws `InvalidStateTransitionException`.

Examples of explicitly rejected transitions:

```
DECLINED    → any state
COMPLETED   → any state
APPROVED    → PROCESSING
OFFER_ACCEPTED → CREATED
```

---

# 7. State Ownership and Enforcement

`application-management-service` is the single source of truth for application state.

External services trigger transitions through two mechanisms:

**A. Direct REST call** — orchestration services call `PATCH /applications/{id}/status` to update state after receiving a synchronous decision.

**B. Kafka event consumption** — domain events published by other services are consumed by `application-management-service` to drive transitions:

| Consumer Class | Event Consumed | Transition |
|----------------|----------------|------------|
| `ESignCompletedEventConsumer` | ESignCompleted | APPROVED → OFFER_ACCEPTED |
| `DocumentsCompletedEventConsumer` | DocumentsCompleted | DOCUMENTS_REQUIRED → UNDERWRITING |

---

# 8. State History and Audit

Every state change is persisted in the `application_event` table with:

- `application_id`
- `event_type`
- `previous_status`
- `new_status`
- `occurred_at`
- `correlation_id`
- `actor`

The `GET /applications/{id}/timeline` endpoint returns all events chronologically, including pre-application events from the invitation flow (joined via `intake_id`). This supports call centre agents reviewing the complete application history.

---

# 9. Applicant Self-Service Access

After the decision phase, applicants access their application via the self-service portal:

1. Applicant submits `applicationId + last4SSN + dateOfBirth` to `POST /applications/login`
2. `application-management-service` verifies identity via `VerificationPort`
3. A JJWT session token is returned (30-minute expiry)
4. The `application-management-ui` shell polls `GET /applications/{id}` every 8 seconds
5. The shell resolves the appropriate screen for the current `applicationStatus` via a declarative navigation configuration (`src/navigation/navigationConfig.js`) and a resolver (`src/navigation/resolveScreen.js`), rather than hardcoded branching — see `openspec/changes/configurable-navigation-flow/`

| Application Status | Screen Shown |
|-------------------|-----------------------|
| APPROVED | `OfferAcceptanceMfe` — declarations + e-sign (application-management-ui) |
| DECLINED | Adverse action information block (application-management-ui) |
| DOCUMENTS_REQUIRED | `<document-upload-manager>` — per-requirement upload slots, progress bar (document-management-ui web component) |
| OFFER_PENDING | `<pricing-offer-selector>` — offer list, selection, hard-pull consent (pricing-offers-ui web component) |
| UNDERWRITING / REFERRED | Under-review spinner |
| OFFER_ACCEPTED / FUNDING_PENDING / FUNDED / COMPLETED | Post-acceptance confirmation block (application-management-ui) |
| All other non-terminal states (CREATED, IN_PROGRESS, READY_FOR_SUBMISSION, SUBMITTED, PROCESSING, SOFT_PULL_PENDING, PRICING_PENDING, CONSENT_CAPTURED, HARD_PULL_PENDING, DECISION_PENDING) | Processing spinner |
| CANCELLED / EXPIRED | Inactive-application block |

Note: this table's status values are the authoritative `ApplicationStatus` enum from `application-management-service` (`domain/application/ApplicationStatus.java`), which includes `READY_FOR_SUBMISSION` and `CONSENT_CAPTURED` not previously listed above, and does not include `STARTED` (superseded by `READY_FOR_SUBMISSION`) or `COMPLIANCE_HOLD` (not currently a real application status — see CLAUDE.md's compliance gate description, which is aspirational pending compliance-orchestration-service implementation).

---

# 10. Concurrency Control

Optimistic locking is used via a `version` column on the `application` table. Conflicting state updates are rejected and retried by the caller.

---

# 11. Related Documents

| Document | Location |
|----------|----------|
| Architecture Overview | `docs/architecture/000-architecture-overview.md` |
| Functional Flow | `docs/flows/01-functional-flow.md` |
| Component Interaction Flows | `docs/flows/02-component-interaction-flows.md` |
| Scenario Flows | `docs/flows/03-scenario-flows.md` |
| Post-Decision OpenSpec | `openspec/changes/post-decision-flow/` |
