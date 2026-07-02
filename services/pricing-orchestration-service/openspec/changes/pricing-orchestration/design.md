## Context

The Personal Loan Acquisition Platform currently orchestrates identity, fraud, and credit evaluations as separate capabilities. There is no capability for generating personalised loan offers based on bureau data, nor for presenting those offers to applicants and capturing their selection and consent before a final credit decision.

Pricing Orchestration introduces a new service (`pricing-orchestration-service`) that coordinates the two-phase credit inquiry model (soft pull → pricing → hard pull → final decision), extends the application state machine with six new states, and interacts with the Decision Platform in two distinct roles: as a Pricing Engine and as a Decision Engine.

The Credit Management Platform is called twice: once for a soft pull (no credit score impact) and once for a hard pull (potential credit score impact, requires explicit consent).

The Offer Management Platform is the system of record for campaign offers (ITA journey). Campaign offer references are already persisted in application management during intake and are included in the pricing request.

---

## Goals / Non-Goals

**Goals:**
- Introduce Pricing Orchestration as a new bounded context with its own service.
- Extend Credit Evaluation Orchestration to support soft pull and hard pull as independent orchestration paths.
- Extend Application Management to persist credit report references, pricing offers, offer selection, and consent records.
- Support decline from every stage of the pricing workflow with a consistent terminal transition.
- Support configurable application expiry and offer-level expiry with re-pricing.
- Support three final decision outcomes: approved, declined, referred.
- Maintain auditability for all state transitions, consent records, and external call outcomes.

**Non-Goals:**
- Generating pricing offers (owned by Decision Platform — Pricing Engine).
- Storing full bureau report data (owned by Credit Management Platform).
- Storing campaign offer master data (owned by Offer Management Platform).
- Executing the final lending decision logic (owned by Decision Platform — Decision Engine).
- Issuing adverse action communications (owned by Notification Orchestration).

---

## Decisions

### Decision 1: Pricing Orchestration is a New Bounded Context

**Decision**: Introduce `pricing-orchestration-service` as a new microservice rather than extending an existing service.

**Rationale**: The pricing workflow spans soft pull, pricing engine interaction, offer presentation, consent capture, hard pull, and final decision routing. This is a cohesive orchestration concern distinct from credit evaluation (which manages bureau inquiry mechanics) and decision orchestration (which manages final decision routing). Co-locating this in either existing service would conflate distinct responsibilities.

**Alternative considered**: Extending `decision-orchestration-service`. Rejected because decision orchestration's concern is final decision routing, not offer generation or consent. Merging them would create a service with two unrelated external dependencies (Pricing Engine and Decision Engine) and unclear state ownership.

---

### Decision 2: Credit Data Flows as a Reference, Not a Payload

**Decision**: The personal loan app persists only the `credit_report_reference_id` from the Credit Management Platform. The Pricing Engine retrieves full bureau data independently using this reference.

**Rationale**: The Credit Management Platform is the system of record for bureau data. Copying bureau data into the application management database violates AP-001 (enterprise platforms remain SOR) and AP-006 (each context owns only data required for acquisition processing). The reference is sufficient for pricing request assembly.

**Alternative considered**: Fetching bureau data in the pricing orchestrator and forwarding it in the pricing request. Rejected because this introduces unnecessary PII flow through the acquisition platform and creates a data duplication risk.

---

### Decision 3: Offer Count is Entirely Engine-Controlled

**Decision**: The platform imposes no minimum or maximum constraint on the number of pricing offers returned. All offers are persisted and presented.

**Rationale**: Pricing models and grids are owned by the Decision Platform. Constraining offer count in the acquisition platform would embed pricing policy where it does not belong (AP-007).

---

### Decision 4: Expired Offers Trigger Re-Pricing, Not Application Decline

**Decision**: When all persisted pricing offers have expired, the system re-initiates soft pull and re-runs pricing rather than declining the application — provided the application itself has not expired.

**Rationale**: Offer expiry is a time-based operational concern, not a credit decision. A decline at this point would be incorrect and damaging to conversion. Re-pricing is the correct recovery.

**Alternative considered**: Declining on offer expiry. Rejected as unnecessarily punitive and misaligned with business intent.

---

### Decision 5: Selected Offer is Immutable After Hard Pull

**Decision**: Once an applicant selects an offer and a hard pull is initiated, the selected offer cannot be changed. If the application is referred to underwriting, the same offer is retained.

**Rationale**: A new offer selection would require a new hard pull. Multiple hard pulls in a short period damage the applicant's credit score. This is a regulatory and UX constraint.

---

### Decision 6: Pricing Orchestration Uses Event-Driven Integration

**Decision**: Soft pull initiation is triggered by the `ApplicationCreated` event. Hard pull initiation is triggered by the `ConsentCaptured` event. All workflow steps publish domain events consumed by downstream capabilities.

**Rationale**: Consistent with the platform's event-driven architecture (EDA). Decouples capabilities, supports retry/recovery, and provides full auditability.

---

### Decision 7: Decline is Terminal at Every Stage

**Decision**: A decline from soft pull, pricing engine, hard pull, or final decision all produce the same terminal `DECLINED` state transition and trigger the adverse action notification workflow.

**Rationale**: Consistent with the existing application state machine terminal state model. Simplifies state management and ensures no application can silently fail without notification.

---

### Decision 8: Offer, Selection, and Consent Data Owned by Pricing Orchestration Service

**Decision**: `pricing-orchestration-service` is the system of record for pricing offers, offer selection, and consent records (`pricing_offers`, `offer_selection`, `consent_records`), including the applicant-facing REST endpoints (`GET /applications/{id}/pricing-offers`, `POST /applications/{id}/offer-selection`, `POST /applications/{id}/consent`). `application-management-service` no longer persists this data.

**Rationale**: `pricing-orchestration-service` already orchestrates the full pricing workflow end-to-end — soft pull, pricing engine call, offer presentation, selection, consent, hard pull. Co-locating offer/consent persistence with the orchestration logic that produces and consumes it avoids the internal-API round-trip that existed under the original design (where `application-management-service` owned this data and `pricing-orchestration-service` pushed offers into it via an internal sync API after every pricing response, and read them back for selection/consent validation). `application-management-service` remains the system of record only for the Application aggregate itself: status, `soft_pull_credit_report_reference_id`, `hard_pull_credit_report_reference_id`, `campaign_offer_id`/`campaign_offer_terms`, and `application_expiry_date`. `pricing-orchestration-service` reads/writes those fields via the existing internal API (`InternalPricingController` / `ApplicationManagementAdapter`) when it needs application-level state.

**Alternative considered**: Keep offer/selection/consent in `application-management-service` with `pricing-orchestration-service` writing to it via internal API and the public endpoints proxying through `application-management-service`. Rejected because it adds a network hop and duplicated validation logic (offer expiry, offer existence) in two services for no ownership benefit — the orchestrator is the only service that produces this data and is best positioned to enforce its invariants (e.g. re-pricing on expiry, consent guarding hard pull).

---

## Risks / Trade-offs

**Re-pricing introduces a second soft pull per session** → Mitigation: Configurable application expiry prevents indefinite re-pricing cycles. Offer expiry dates from the pricing engine act as a natural throttle.

**Decision Platform called twice (as Pricing Engine and Decision Engine)** → Risk: Tight coupling to a single external system for two distinct concerns. Mitigation: Each call uses a separate adapter and a separate circuit breaker configuration. If the Decision Platform separates into two systems in future, adapters can be updated independently.

**Consent record must be immutable** → Risk: Accidental update or delete of consent data. Mitigation: Consent records are append-only. No update or delete operations exposed. Retained for 7 years per data retention policy.

**Hard pull failure after consent** → Risk: Applicant gave consent and expected a decision but hard pull fails. Mitigation: Hard pull failure transitions to `DECLINED` with appropriate notification. The consent record is retained for audit even on failure.

**Referred applicants retain expired selected offer** → Risk: If underwriting review takes longer than the offer expiry, the confirmed offer may be expired by the time of manual approval. Mitigation: Open question — see below.

---

## Migration Plan

1. Deploy `application-service` schema migration: new state values, new columns for credit report references and campaign offer reference on the application table.
2. Deploy `credit-evaluation-service` update: add soft pull and hard pull as independent orchestration paths.
3. Deploy `pricing-orchestration-service`: new service consuming `ApplicationCreated` and `ConsentCaptured` events, with its own schema migration creating `pricing_offers`, `offer_selection`, and `consent_records` tables (see Decision 8) and its own public offer/selection/consent API.
4. Update `application-service` event publishing: publish `ApplicationCreated` event on successful application creation.
5. Remove the superseded `pricing_offers`, `offer_selection`, and `consent_records` tables and endpoints from `application-service`: add a new Flyway migration in `application-service` that `DROP TABLE IF EXISTS`s these three tables. Old migration files that originally created them are left untouched (Flyway history is immutable); the drop is a new forward migration. No production data exists yet in this environment, so a destructive drop is acceptable.
6. No changes to existing in-flight applications. New states only apply to applications created after deployment.
7. Rollback: pricing orchestration service can be disabled by stopping event consumption. Existing applications fall back to prior flow.

---

## Open Questions

1. **Offer expiry during underwriting review**: If an underwriting review takes longer than the offer expiry date on the selected pricing offer, should the offer be re-validated before manual approval, or is the selected offer locked at the point of hard pull regardless of its expiry date?

2. **Re-pricing limit**: Should there be a maximum number of re-pricing cycles allowed within a single application session before declining? If so, what is the limit?

3. **Partial bureau availability**: If the pricing engine cannot access all three bureaus, should it proceed with available data or wait and retry? This is a Decision Platform concern but the orchestrator needs to handle the response.

4. **Audit requirements for pricing model reference**: Is `pricing_model_ref` returned by the Decision Platform sufficient for regulatory audit, or does the platform need to capture additional metadata from each pricing engine response?
