## MODIFIED Requirements

### Requirement: Application State Machine Extended for Pricing Lifecycle
The application state machine SHALL include new states to represent the pricing orchestration lifecycle. All existing states and transitions remain valid.

#### Scenario: Application transitions through pricing states
- **WHEN** the pricing orchestration workflow progresses
- **THEN** the application SHALL transition through states in the following order: `SOFT_PULL_PENDING` → `PRICING_PENDING` → `OFFER_PENDING` → `CONSENT_CAPTURED` → `HARD_PULL_PENDING` → `DECISION_PENDING` → `APPROVED` or `DECLINED` or `REFERRED`

#### Scenario: Decline transition valid from any pricing state
- **WHEN** a decline occurs at `SOFT_PULL_PENDING`, `PRICING_PENDING`, `HARD_PULL_PENDING`, or `DECISION_PENDING`
- **THEN** the application SHALL transition directly to `DECLINED`

#### Scenario: Expiry transition valid from any pricing state
- **WHEN** an application expiry check fails during any pricing state
- **THEN** the application SHALL transition directly to `EXPIRED`

---

## ADDED Requirements

### Requirement: Credit Report Reference Persistence
The system SHALL persist the credit report reference identifier returned by the Credit Management Platform for both soft pull and hard pull inquiries.

#### Scenario: Soft pull reference persisted
- **WHEN** a soft pull completes successfully
- **THEN** the system SHALL store `soft_pull_credit_report_reference_id` against the application record

#### Scenario: Hard pull reference persisted
- **WHEN** a hard pull completes successfully
- **THEN** the system SHALL store `hard_pull_credit_report_reference_id` against the application record

---

> **Note**: Pricing offer, offer selection, and consent record persistence are owned by `pricing-orchestration-service`, not `application-management-service` — see the `pricing-orchestration` capability spec (Decision 8 in design.md). `application-management-service` retains only the Application aggregate itself: status, credit report reference IDs, and campaign offer reference.

### Requirement: Campaign Offer Reference Persistence
The system SHALL persist the campaign offer reference from the Offer Management Platform during ITA intake so it is available for the pricing request assembly.

#### Scenario: Campaign offer reference stored at intake
- **WHEN** an ITA journey application is created
- **THEN** the system SHALL persist `campaign_offer_id` and `campaign_offer_terms` from the Offer Management Platform against the application record

#### Scenario: No campaign offer reference for direct journey
- **WHEN** a direct journey application is created
- **THEN** the `campaign_offer_id` field SHALL be null
