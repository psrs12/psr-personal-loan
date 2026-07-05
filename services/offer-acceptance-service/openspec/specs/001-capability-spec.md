# 001 - Offer Acceptance Capability Specification

Version: 1.0

Status: Active

Service: offer-acceptance-service

Port: 8085

---

# 1. Purpose

The Offer Acceptance service owns the post-approval e-sign and declaration capture flow for a personal loan application.

When an application receives an APPROVED final decision, this service:

- Creates an `OfferAcceptanceSession` containing the standard loan declarations
- Exposes the declarations to the applicant for review
- Captures the applicant's e-sign acceptance of all mandatory declarations
- Records the e-sign with IP address and timestamp
- Publishes an `ESignCompleted` event that drives `application-management-service` to transition the application to `OFFER_ACCEPTED`

---

# 2. Bounded Context

Offer Acceptance

The service does not own application state. State transitions are owned exclusively by `application-management-service`.

---

# 3. Responsibilities

| Responsibility | Owned |
|----------------|-------|
| OfferAcceptanceSession lifecycle | Yes |
| Declaration definition (STANDARD_DECLARATIONS) | Yes |
| ESign capture and validation | Yes |
| ESignCompleted event publication | Yes |
| Application state transitions | No — delegated to application-management-service |
| Offer pricing and decision | No |
| Document collection | No |

---

# 4. Trigger

The service is triggered by the `FinalDecisionApproved` Kafka event published by `pricing-orchestration-service`.

On receipt, a new `OfferAcceptanceSession` is created for the application with status `PENDING`.

---

# 5. Workflow Summary

```
FinalDecisionApproved (Kafka)
  → CreateSessionUseCase
  → OfferAcceptanceSession created (status: PENDING)

GET /applications/{id}/declarations
  → Return declarations list from session

POST /applications/{id}/esign
  → ESignUseCase
  → Validate all mandatory declarations accepted
  → session.sign() → ESignRecord created
  → OfferAcceptanceSession status → SIGNED
  → Publish ESignCompleted (Kafka)
  → application-management-service transitions to OFFER_ACCEPTED
```

---

# 6. Declarations

Five standard declarations are defined in `OfferAcceptanceSession.STANDARD_DECLARATIONS`:

| Declaration Type | Title | Mandatory |
|-----------------|-------|-----------|
| `TERMS_AND_CONDITIONS` | Terms and Conditions | Yes |
| `PRIVACY_POLICY` | Privacy Policy | Yes |
| `CREDIT_REPORTING` | Credit Reporting Consent | Yes |
| `ELECTRONIC_SIGNATURE` | Electronic Signature Consent | Yes |
| `MARKETING` | Marketing Communications | No |

All mandatory declarations must be present in the e-sign request. Marketing is optional.

---

# 7. Session Status Values

| Status | Meaning |
|--------|---------|
| `PENDING` | Session created, awaiting e-sign |
| `SIGNED` | E-sign successfully captured |

---

# 8. Related Documents

```
openspec/offer-acceptance/spec.md                  — platform capability spec
openspec/changes/post-decision-flow/specs/offer-acceptance/spec.md  — change delta spec
services/offer-acceptance-service/openspec/specs/02-domain-model.md
services/offer-acceptance-service/openspec/specs/04-sequence-diagrams.md
services/offer-acceptance-service/openspec/specs/05-api-contracts.md
services/offer-acceptance-service/openspec/specs/06-persistence-model.md
services/offer-acceptance-service/openspec/specs/07-acceptance-tests.md
```
