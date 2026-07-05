# Acceptance Tests

# Pricing Orchestration Service

Version: 1.0

---

# Feature: Soft Pull and Offer Pricing

## Scenario 1 — Soft pull initiated on ApplicationCreated

**Given** an `ApplicationCreatedEvent` is received for `APP123`

**When** `ApplicationCreatedEventConsumer` processes the event

**Then** the application status shall transition to `SOFT_PULL_PENDING`

**And** a soft pull request shall be submitted to the Credit Management Platform with `applicationId: APP123`

**And** a `SoftPullInitiated` event shall be published and the Credit Management soft pull reference persisted

**And** the application status shall transition to `PRICING_PENDING`

**And** a `SoftPullCompleted` event shall be published

**And** `PricingRequestAssemblyService.requestPricing` shall be called to assemble and submit the pricing request

---

## Scenario 2 — Offers generated and persisted

**Given** the Decision Platform returns `outcome: OFFERS_GENERATED` with two offer objects

**When** `SoftPullOrchestrationService` processes the response

**Then** two `PricingOffer` records shall be persisted for `APP123`

**And** the offers shall be available for retrieval by the UI

---

## Scenario 3 — Soft pull pricing declined

**Given** the Decision Platform returns `outcome: DECLINED` with `declineReasonCode: INSUFFICIENT_INCOME`

**When** `SoftPullOrchestrationService` processes the response

**Then** the application state shall transition to `DECLINED` via `application-management-service`

**And** no `PricingOffer` records shall be persisted

---

## Scenario 3a — Credit Management unavailable during soft pull

**Given** an `ApplicationCreatedEvent` is received for `APP123`

**And** the Credit Management Platform throws an exception (timeout or error)

**When** `SoftPullOrchestrationService` processes the event

**Then** the application state shall transition to `DECLINED`

**And** a `SoftPullFailed` event shall be published with the failure reason

**And** `PricingRequestAssemblyService.requestPricing` shall NOT be called

---

# Feature: Offer Selection and Hard Pull

## Scenario 4 — Hard pull initiated on ConsentCaptured

**Given** a `ConsentCapturedEvent` is received for `APP123` with `selectedPricingOfferId: PO-001`

**When** `ConsentCapturedEventConsumer` processes the event

**Then** a hard pull request shall be submitted to the Credit Management Platform

**And** the `PricingOffer` with id `PO-001` shall be marked `selected: true`

---

# Feature: Final Decision Routing

## Scenario 5 — Final decision APPROVED

**Given** a `HardPullCompletedEvent` is received for `APP123`

**When** `HardPullOrchestrationService` submits the final decision and receives `outcome: APPROVED`

**Then** the application state shall transition to `APPROVED` via `application-management-service`

**And** a `FinalDecisionApproved` event shall be published with `applicationId` and `selectedOfferId`

---

## Scenario 6 — Final decision DECLINED

**Given** the Decision Platform returns `outcome: DECLINED` with `reasonCode: CREDIT_RISK`

**When** `HardPullOrchestrationService` processes the response

**Then** the application state shall transition to `DECLINED`

**And** a `FinalDecisionDeclined` event shall be published with `reasonCode: CREDIT_RISK`

---

## Scenario 7 — Final decision REFERRED

**Given** the Decision Platform returns `outcome: REFERRED`

**When** `HardPullOrchestrationService` processes the response

**Then** the application state shall transition to `REFERRED`

**And** a `FinalDecisionReferred` event shall be published

**And** the selected offer shall be retained unchanged

---

## Scenario 8 — Final decision DOCUMENTS_REQUIRED

**Given** the Decision Platform returns `outcome: DOCUMENTS_REQUIRED` with `documents: [{decisionEngineCode: "BANK_STMT_3M", count: 1}]`

**When** `HardPullOrchestrationService` processes the response

**Then** the application state shall transition to `DOCUMENTS_REQUIRED`

**And** a `FinalDecisionDocumentsRequired` event shall be published with the verbatim `documents` array

**And** the service shall NOT modify or interpret the `decisionEngineCode` values

---

## Scenario 9 — Credit Management unavailable during hard pull

**Given** a `ConsentCapturedEvent` is received for `APP123`

**And** the Credit Management Platform throws an exception during hard pull initiation

**When** `HardPullOrchestrationService` processes the event

**Then** the application state shall transition to `DECLINED`

**And** a `HardPullFailed` event shall be published with the failure reason

**And** the Decision Platform shall NOT be called for a final decision

---

# Non-Functional Acceptance Criteria

## Performance

- Soft pull initiation shall complete within 2 seconds (excluding Credit Management Platform latency)
- Final decision routing shall complete within 500ms of receiving the Decision Platform response

## Idempotency

- Duplicate `ApplicationCreatedEvent` events shall not result in duplicate soft pull requests

## Security

- Applicant reference passed to Credit Management Platform must not be a raw SSN
- No PII in log output

---

# Related Documents

```
services/pricing-orchestration-service/openspec/specs/001-capability-spec.md
services/pricing-orchestration-service/openspec/specs/02-domain-model.md
services/pricing-orchestration-service/openspec/specs/04-sequence-diagrams.md
services/pricing-orchestration-service/openspec/specs/05-api-contracts.md
services/pricing-orchestration-service/openspec/specs/06-persistence-model.md
openspec/pricing-orchestration/spec.md
```
