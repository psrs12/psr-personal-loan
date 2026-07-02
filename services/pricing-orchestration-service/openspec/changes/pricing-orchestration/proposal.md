## Why

The Personal Loan Acquisition Platform currently has no capability to evaluate applicant creditworthiness and generate personalised loan offers before presenting them to the applicant. Pricing Orchestration introduces a soft pull / pricing / hard pull workflow that enables the platform to present credit-bureau-informed loan offers to applicants, capture their selection and consent, and obtain a final lending decision — supporting straight-through processing for approved applicants and controlled decline and referral paths for others.

## What Changes

- Introduce a new **Pricing Orchestration** capability responsible for coordinating the end-to-end offer generation and credit decision workflow.
- Extend **Credit Evaluation Orchestration** to support both soft pull (triggered on application creation) and hard pull (triggered after offer selection and consent).
- Extend **Application Management** to persist credit report references and campaign offer reference on the Application aggregate. **Pricing Orchestration** owns and persists pricing offers, offer selection, and hard pull/offer-acceptance consent directly (see design.md Decision 8), including the applicant-facing endpoints for retrieving offers, submitting selection, and capturing consent.
- Add new application states: `SOFT_PULL_PENDING`, `PRICING_PENDING`, `OFFER_PENDING`, `CONSENT_CAPTURED`, `HARD_PULL_PENDING`, `DECISION_PENDING`.
- Add decline flow from pricing stage onward: pricing decline, hard pull decline, and final decision decline all transition the application to `DECLINED`.
- Add referred flow after final decision: application moves to `REFERRED` for manual underwriting review.
- Add configurable application expiry and offer-level expiry with re-pricing logic when offers expire during applicant offer selection.

## Capabilities

### New Capabilities

- `pricing-orchestration`: End-to-end orchestration of soft pull initiation, pricing request assembly and submission to the Decision Platform (Pricing Engine), pricing offer persistence, offer presentation, offer selection, consent capture, hard pull initiation, and final decision routing (approved / declined / referred). Owns the `pricing_offers`, `offer_selection`, and `consent_records` tables and the public offer/selection/consent endpoints.

### Modified Capabilities

- `credit-evaluation-orchestration`: Extended to support two distinct credit bureau inquiry types — soft pull (triggered by `ApplicationCreated` event, no credit impact) and hard pull (triggered by `ConsentCaptured` event, credit impact). Existing capability only defines a single credit evaluation step.
- `application-management`: Extended to persist credit report reference and campaign offer/expiry metadata on the Application aggregate. State machine extended with new pricing lifecycle states. Pricing offers, offer selection, and consent records are NOT persisted here — see `pricing-orchestration`.

## Impact

- **New service**: `pricing-orchestration-service` — consumes domain events, orchestrates soft pull, pricing, consent, hard pull, and decision flows; owns its own `pricing_offers`, `offer_selection`, and `consent_records` persistence and exposes the public offer/selection/consent REST API.
- **Extended service**: `credit-evaluation-service` — adds soft pull and hard pull as separate orchestration paths.
- **Extended service**: `application-service` — new state transitions; retains only credit report reference and campaign offer reference persistence on the Application aggregate. The `pricing_offers`, `offer_selection`, and `consent_records` tables and their public endpoints are removed from this service (dropped via Flyway migration) since no production data exists yet in this environment.
- **External system dependency**: Decision Platform called twice — once as Pricing Engine (after soft pull), once as Decision Engine (after hard pull).
- **External system dependency**: Credit Management Platform called twice — once for soft pull, once for hard pull.
- **External system dependency**: Offer Management Platform — campaign offer reference read during ITA intake and included in pricing request.
- **New domain events**: `SoftPullInitiated`, `SoftPullCompleted`, `SoftPullFailed`, `PricingRequested`, `PricingOffersReceived`, `PricingDeclined`, `OfferSelected`, `ConsentCaptured`, `HardPullInitiated`, `HardPullCompleted`, `HardPullFailed`, `FinalDecisionApproved`, `FinalDecisionDeclined`, `FinalDecisionReferred`.
- **Application state machine**: Six new states added, new terminal transitions for pricing decline and decision decline.
- **API**: New endpoints for offer retrieval, offer selection, and consent capture.
