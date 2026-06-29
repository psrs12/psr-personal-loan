## 1. Database Schema — Application Service

- [x] 1.1 Add new application states to state enum: `SOFT_PULL_PENDING`, `PRICING_PENDING`, `OFFER_PENDING`, `CONSENT_CAPTURED`, `HARD_PULL_PENDING`, `DECISION_PENDING`
- [x] 1.2 Add `soft_pull_credit_report_reference_id` and `hard_pull_credit_report_reference_id` columns to application table
- [x] 1.3 Add `campaign_offer_id` and `campaign_offer_terms` (JSONB) columns to application table
- [x] 1.4 Create `pricing_offers` table with columns: `pricing_offer_id`, `application_id`, `approved_amount`, `interest_rate`, `apr`, `term_months`, `monthly_repayment`, `total_repayable`, `offer_expiry_date`, `pricing_model_ref`, `bureau_snapshot_ref`, `offer_status`, `created_at`
- [x] 1.5 Create `offer_selection` table with columns: `id`, `application_id`, `selected_pricing_offer_id`, `offer_selected_timestamp`
- [x] 1.6 Create `consent_records` table with columns: `id`, `application_id`, `consent_type`, `consent_given_at`, `consent_channel`, `applicant_reference`, `selected_pricing_offer_id`
- [x] 1.7 Add `application_expiry_config` table with columns: `product_type`, `channel`, `expiry_threshold_days`
- [x] 1.8 Write and validate all Flyway migration scripts

## 2. Application Service — State Machine Extension

- [x] 2.1 Add new state transitions to application state machine: `SUBMITTED` → `SOFT_PULL_PENDING`, `SOFT_PULL_PENDING` → `PRICING_PENDING`, `PRICING_PENDING` → `OFFER_PENDING`, `OFFER_PENDING` → `CONSENT_CAPTURED`, `CONSENT_CAPTURED` → `HARD_PULL_PENDING`, `HARD_PULL_PENDING` → `DECISION_PENDING`, `DECISION_PENDING` → `APPROVED` / `DECLINED` / `REFERRED`
- [x] 2.2 Add decline transitions from `SOFT_PULL_PENDING`, `PRICING_PENDING`, `HARD_PULL_PENDING`, `DECISION_PENDING` → `DECLINED`
- [x] 2.3 Add expiry transition from any pricing state → `EXPIRED`
- [x] 2.4 Implement application expiry check using configurable threshold from `application_expiry_config`
- [x] 2.5 Add `ApplicationCreated` event publishing on successful application creation
- [x] 2.6 Write unit tests for all new state transitions including invalid transition rejections

## 3. Application Service — Pricing Data APIs

- [x] 3.1 Implement `GET /applications/{applicationId}/pricing-offers` endpoint — retrieves active non-expired pricing offers, triggers re-pricing if all offers expired
- [x] 3.2 Implement `POST /applications/{applicationId}/offer-selection` endpoint — validates offer exists and is not expired, persists selection
- [x] 3.3 Implement `POST /applications/{applicationId}/consent` endpoint — persists hard pull consent and offer acceptance consent records, publishes `ConsentCaptured` event
- [x] 3.4 Add offer status management: `ACTIVE`, `SUPERSEDED`, `EXPIRED`
- [x] 3.5 Add campaign offer reference storage on application creation (ITA journey — nullable for direct journey)
- [x] 3.6 Write integration tests for all new endpoints

## 4. Credit Evaluation Service — Soft Pull and Hard Pull

- [x] 4.1 Implement soft pull orchestration triggered by `ApplicationCreated` event consumer
- [x] 4.2 Implement Credit Management Platform adapter method for soft pull inquiry
- [x] 4.3 Persist `soft_pull_credit_report_reference_id` to application management on soft pull success
- [x] 4.4 Publish `SoftPullCompleted` event on soft pull success
- [x] 4.5 Publish `SoftPullFailed` event and trigger application decline on soft pull failure
- [x] 4.6 Implement hard pull orchestration triggered by `ConsentCaptured` event consumer
- [x] 4.7 Implement Credit Management Platform adapter method for hard pull inquiry
- [x] 4.8 Persist `hard_pull_credit_report_reference_id` to application management on hard pull success
- [x] 4.9 Publish `HardPullCompleted` event on hard pull success
- [x] 4.10 Publish `HardPullFailed` event and trigger application decline on hard pull failure
- [x] 4.11 Implement consent guard: hard pull SHALL NOT be initiated without valid hard pull consent record
- [x] 4.12 Write unit tests for both pull types and failure paths
- [x] 4.13 Write integration tests with Testcontainers for Credit Management Platform adapter

## 5. Pricing Orchestration Service — New Service

- [x] 5.1 Scaffold `pricing-orchestration-service` following existing service structure and guidelines
- [x] 5.2 Implement `SoftPullCompleted` event consumer
- [x] 5.3 Implement pricing request assembly: read application details, `soft_pull_credit_report_reference_id`, and campaign offer reference (if present) from application management
- [x] 5.4 Implement Decision Platform adapter for pricing engine call
- [x] 5.5 Implement pricing response handler: persist all returned pricing offers with status `ACTIVE`
- [x] 5.6 Publish `PricingOffersReceived` event on successful pricing response
- [x] 5.7 Handle pricing engine decline response: publish `PricingDeclined`, transition application to `DECLINED`
- [x] 5.8 Implement re-pricing trigger: when offer retrieval finds all offers expired, initiate new soft pull via Credit Evaluation Service
- [x] 5.9 Mark superseded offers as `SUPERSEDED` before persisting new offers on re-pricing
- [x] 5.10 Write unit tests for pricing request assembly (ITA and direct journey variants)
- [x] 5.11 Write unit tests for offer persistence, decline handling, and re-pricing
- [x] 5.12 Write integration tests with Testcontainers for Decision Platform pricing adapter

## 6. Decision Orchestration Service — Final Decision

- [x] 6.1 Implement `HardPullCompleted` event consumer
- [x] 6.2 Implement final decision request assembly: `application_id`, `selected_pricing_offer_id`, `hard_pull_credit_report_reference_id`
- [x] 6.3 Implement Decision Platform adapter for final decision call
- [x] 6.4 Handle approved response: transition application to `APPROVED`, publish `FinalDecisionApproved`, trigger document collection
- [x] 6.5 Handle declined response: transition application to `DECLINED`, publish `FinalDecisionDeclined`, trigger adverse action notification
- [x] 6.6 Handle referred response: transition application to `REFERRED`, retain selected offer unchanged, publish `FinalDecisionReferred`, trigger underwriting workflow
- [x] 6.7 Write unit tests for all three decision outcome handlers
- [x] 6.8 Write integration tests with Testcontainers for Decision Platform decision adapter

## 7. Underwriting Service — Referred Path

- [ ] 7.1 Implement `FinalDecisionReferred` event consumer in underwriting service
- [ ] 7.2 Ensure underwriting work item includes selected pricing offer reference (read-only)
- [ ] 7.3 Implement underwriter action: request additional documentation (triggers document collection workflow)
- [ ] 7.4 Implement underwriter action: approve — transition application to `APPROVED`, confirm selected offer
- [ ] 7.5 Implement underwriter action: decline — transition application to `DECLINED`, trigger adverse action notification
- [ ] 7.6 Write unit tests for referred path handlers

## 8. Domain Events

- [x] 8.1 Define and register all new domain events: `SoftPullInitiated`, `SoftPullCompleted`, `SoftPullFailed`, `PricingRequested`, `PricingOffersReceived`, `PricingDeclined`, `OfferSelected`, `ConsentCaptured`, `HardPullInitiated`, `HardPullCompleted`, `HardPullFailed`, `FinalDecisionApproved`, `FinalDecisionDeclined`, `FinalDecisionReferred`
- [x] 8.2 Register Kafka topics for all new events
- [ ] 8.3 Implement event schema validation for all new event types
- [ ] 8.4 Write contract tests for all new event schemas

## 9. Notification Orchestration — Adverse Action

- [ ] 9.1 Implement `PricingDeclined` event consumer: trigger adverse action notification
- [ ] 9.2 Implement `FinalDecisionDeclined` event consumer: trigger adverse action notification
- [ ] 9.3 Implement `SoftPullFailed` event consumer: trigger adverse action notification
- [ ] 9.4 Implement `HardPullFailed` event consumer: trigger adverse action notification
- [ ] 9.5 Write unit tests for each decline notification trigger

## 10. Acceptance Tests

- [ ] 10.1 ITA journey: soft pull → pricing → offer selection → consent → hard pull → approved → document collection triggered
- [ ] 10.2 Direct journey: soft pull → pricing (no campaign offer) → offer selection → consent → hard pull → approved
- [ ] 10.3 Pricing decline: soft pull → pricing engine returns decline → application DECLINED → adverse action notification
- [ ] 10.4 Hard pull failure: consent captured → hard pull fails → application DECLINED → adverse action notification
- [ ] 10.5 Final decision declined: hard pull success → decision declined → application DECLINED → adverse action notification
- [ ] 10.6 Final decision referred: hard pull success → decision referred → underwriting triggered → selected offer retained
- [ ] 10.7 Offer expiry re-pricing: all offers expired → new soft pull → new pricing → new offers presented
- [ ] 10.8 Application expiry: application exceeds configured threshold → application EXPIRED
- [ ] 10.9 Consent guard: hard pull not initiated without consent record
- [ ] 10.10 Referred + underwriter approve: underwriting approval → application APPROVED → selected offer confirmed
- [ ] 10.11 Referred + underwriter decline: underwriting decline → application DECLINED → adverse action notification
- [ ] 10.12 Referred + additional docs: underwriter requests bank statements → document collection triggered
