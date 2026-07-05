# 001 - Pricing Orchestration Capability Specification

Version: 1.0

Status: Active

Service: pricing-orchestration-service

Port: 8082

---

# 1. Purpose

The Pricing Orchestration service coordinates the credit evaluation and decision workflow for a personal loan application.

It orchestrates the sequence: soft pull → offer pricing → offer selection → hard pull → final decision routing. It does not own credit policy, offer pricing logic, or decisioning rules — those belong to enterprise platforms. The service sequences the workflow, adapts external responses to platform domain events, and publishes the correct outcome events.

---

# 2. Bounded Context

Pricing Orchestration

---

# 3. Responsibilities

| Responsibility | Owned |
|----------------|-------|
| Soft pull initiation and result handling | Yes |
| Pricing request assembly and submission | Yes |
| Offer persistence (PricingOffer records) | Yes |
| Offer selection and consent capture | Yes |
| Hard pull initiation and result handling | Yes |
| Final decision routing (APPROVED/DECLINED/REFERRED/DOCUMENTS_REQUIRED) | Yes |
| Domain event publication for all outcomes | Yes |
| Credit policy and scoring | No — Credit Management Platform |
| Offer generation and pricing algorithms | No — Decision Platform (Pricing Engine) |
| Decisioning rules | No — Decision Platform |
| Application state transitions | No — application-management-service |

---

# 4. Trigger

The service is triggered by two Kafka events:

1. `ApplicationCreatedEvent` — initiates the soft pull
2. `ConsentCapturedEvent` — initiates the hard pull after offer selection

---

# 5. Workflow Summary

```
ApplicationCreatedEvent (Kafka)
  → SoftPullOrchestrationService
  → CreditManagementPort: initiateSoftPull(applicationId, applicantReference)
  → SoftPullCompleted received
  → PricingRequestAssemblyService: assemble PricingRequest
  → DecisionPlatformPort: submitPricingRequest(PricingRequest)
  → PricingEngineResponse: OFFERS_GENERATED | DECLINED
  → If OFFERS_GENERATED: persist PricingOffers, publish offers to UI

ConsentCapturedEvent (Kafka) — applicant selected offer + consented to hard pull
  → HardPullOrchestrationService
  → CreditManagementPort: initiateHardPull(applicationId, applicantReference, selectedPricingOfferId)
  → HardPullCompleted received
  → DecisionPlatformPort: submitFinalDecision(applicationId, selectedOfferId, hardPullRef)
  → FinalDecisionResponse: APPROVED | DECLINED | REFERRED | DOCUMENTS_REQUIRED
  → Route outcome → publish domain event
```

---

# 6. Final Decision Outcomes

| Outcome | Application State | Event Published |
|---------|------------------|-----------------|
| `APPROVED` | `APPROVED` | `FinalDecisionApproved` |
| `DECLINED` | `DECLINED` | `FinalDecisionDeclined` |
| `REFERRED` | `REFERRED` | `FinalDecisionReferred` |
| `DOCUMENTS_REQUIRED` | `DOCUMENTS_REQUIRED` | `FinalDecisionDocumentsRequired` |

`FinalDecisionDocumentsRequired` carries Decision Engine codes verbatim — this service does NOT map them.

---

# 7. External Integrations

| System | Port Interface | Operations |
|--------|---------------|-----------|
| Credit Management Platform | `CreditManagementPort` | `initiateSoftPull()`, `initiateHardPull()` |
| Decision Platform (Pricing Engine) | `DecisionPlatformPort` | `submitPricingRequest()`, `submitFinalDecision()` |
| Application Management Service | `ApplicationManagementPort` | `updateApplicationStatus()` |

---

# 8. Related Documents

```
openspec/pricing-orchestration/spec.md           — platform capability spec
openspec/changes/post-decision-flow/specs/pricing-orchestration/spec.md
services/pricing-orchestration-service/openspec/specs/02-domain-model.md
services/pricing-orchestration-service/openspec/specs/04-sequence-diagrams.md
services/pricing-orchestration-service/openspec/specs/05-api-contracts.md
services/pricing-orchestration-service/openspec/specs/06-persistence-model.md
services/pricing-orchestration-service/openspec/specs/07-acceptance-tests.md
```
