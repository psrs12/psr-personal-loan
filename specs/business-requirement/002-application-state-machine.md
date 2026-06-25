# application-state-machine.md

# Personal Loan Acquisition

## Application State Machine Specification

Version: 1.0

Status: Draft

Owner: Personal Loan Acquisition Platform

---

# 1. Purpose

This document defines the standard application lifecycle states and state transition rules for the Personal Loan Acquisition Platform.

All acquisition modules must follow this state model.

The state machine ensures:

* Consistent application processing
* Controlled workflow progression
* Auditability
* Error handling
* Recovery from failures
* Clear ownership boundaries

---

# 2. Scope

## Applies To

* Invitation To Apply
* Application Management
* Identity Verification Orchestration
* Fraud Verification Orchestration
* Credit Evaluation Orchestration
* Decision Orchestration
* Offer Acceptance
* Document Collection
* Underwriting
* Bank Verification
* Funding Request

---

# 3. Application Lifecycle Overview

```text
CREATED

   |
   v

STARTED

   |
   v

IN_PROGRESS

   |
   v

SUBMITTED

   |
   v

PROCESSING

   |
   +----------------+
   |                |
   v                v

APPROVED        DECLINED
   |
   v

OFFER_PENDING

   |
   v

OFFER_ACCEPTED

   |
   v

DOCUMENT_PENDING

   |
   v

UNDERWRITING

   |
   +---------------+
   |               |
   v               v

APPROVED        REFERRED

   |
   v

FUNDING_PENDING

   |
   v

FUNDED

   |
   v

COMPLETED
```

---

# 4. State Definitions

## CREATED

Application record exists.

Entry points:

* Invitation flow
* Direct application flow

Conditions:

* Application ID generated
* Minimal data available

---

## STARTED

Customer has entered application journey.

Required:

* Application ID
* Applicant reference
* Source channel

---

## IN_PROGRESS

Customer is actively completing application.

Allowed:

* Update information
* Save progress
* Resume later

---

## SUBMITTED

Customer has completed required information.

Triggers:

* Identity verification
* Fraud verification
* Credit evaluation

---

## PROCESSING

Application is undergoing enterprise evaluations.

Includes:

* Identity checks
* Fraud verification
* Credit evaluation
* Decision processing

---

## APPROVED

Decision Platform returned approval.

Next:

Offer presentation

---

## DECLINED

Decision Platform returned decline.

Terminal state.

---

## REFERRED

Application requires manual processing.

Examples:

* Manual underwriting
* Additional verification
* Exception review

---

## OFFER_PENDING

Approved application awaiting offer selection.

---

## OFFER_ACCEPTED

Customer accepted offer.

Triggers:

* Document collection
* Funding preparation

---

## DOCUMENT_PENDING

Required documents are outstanding.

---

## UNDERWRITING

Manual review is in progress.

---

## FUNDING_PENDING

Funding request submitted.

Waiting for funding completion.

---

## FUNDED

Loan funding completed.

---

## COMPLETED

Application lifecycle complete.

---

# 5. Terminal States

The following are terminal:

```text
DECLINED

FUNDED

COMPLETED

CANCELLED

EXPIRED
```

---

# 6. State Transition Rules

| Current State    | Next State       | Trigger                |
| ---------------- | ---------------- | ---------------------- |
| CREATED          | STARTED          | Applicant begins       |
| STARTED          | IN_PROGRESS      | Data entered           |
| IN_PROGRESS      | SUBMITTED        | Applicant submits      |
| SUBMITTED        | PROCESSING       | Validation complete    |
| PROCESSING       | APPROVED         | Decision approved      |
| PROCESSING       | DECLINED         | Decision declined      |
| PROCESSING       | REFERRED         | Manual review required |
| APPROVED         | OFFER_PENDING    | Offer requested        |
| OFFER_PENDING    | OFFER_ACCEPTED   | Customer accepts       |
| OFFER_ACCEPTED   | DOCUMENT_PENDING | Documents required     |
| DOCUMENT_PENDING | UNDERWRITING     | Review needed          |
| UNDERWRITING     | APPROVED         | Review complete        |
| UNDERWRITING     | REFERRED         | Additional review      |
| APPROVED         | FUNDING_PENDING  | Funding requested      |
| FUNDING_PENDING  | FUNDED           | Funding complete       |
| FUNDED           | COMPLETED        | Final completion       |

---

# 7. Invalid Transitions

The system must reject:

```
DECLINED → PROCESSING

FUNDED → IN_PROGRESS

COMPLETED → SUBMITTED

OFFER_ACCEPTED → CREATED

CANCELLED → PROCESSING
```

---

# 8. Ownership Rules

Application Service owns:

* Application state
* State transition validation
* State history

External platforms own:

Identity Verification Status

Fraud Result

Credit Result

Decision Result

Funding Result

External results update application state through events.

---

# 9. State Change Events

## Published Events

ApplicationCreated

ApplicationStarted

ApplicationUpdated

ApplicationSubmitted

ApplicationProcessingStarted

ApplicationApproved

ApplicationDeclined

ApplicationReferred

OfferAccepted

DocumentsCompleted

UnderwritingCompleted

FundingRequested

FundingCompleted

ApplicationCompleted

---

# 10. Event Consumers

Identity Service

Fraud Service

Credit Service

Decision Service

Offer Acceptance Service

Document Service

Underwriting Service

Funding Service

Notification Service

---

# 11. State Persistence

Application table:

```
application_id

current_state

previous_state

state_changed_time

state_reason

updated_by

version
```

---

# 12. Concurrency Control

The system shall prevent conflicting updates.

Approach:

Optimistic locking

Version column

Example:

```
version = 5

Update allowed only if version = 5
```

---

# 13. Recovery Rules

If external service fails:

Application remains in current state.

Example:

Credit service unavailable:

```
PROCESSING

reason:

CREDIT_PENDING
```

Retry occurs asynchronously.

---

# 14. Timeout Rules

Examples:

Identity verification timeout:

Move to:

IDENTITY_REVIEW_PENDING

Decision timeout:

Move to:

DECISION_PENDING

Funding timeout:

Move to:

FUNDING_PENDING

---

# 15. Audit Requirements

Every state change must capture:

Application ID

Previous State

New State

Timestamp

Actor

Reason

Correlation ID

---

# 16. Related Specifications

000-domain-boundaries-and-context-map.md

001-acquisition-business-capabilities.md

002-invitation-to-apply-spec.md

003-application-spec.md

004-identity-verification-orchestration-spec.md

005-fraud-verification-orchestration-spec.md

006-credit-evaluation-orchestration-spec.md

007-decision-orchestration-spec.md
