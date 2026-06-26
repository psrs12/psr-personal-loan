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

### Requirement: Pricing Offers Persistence
The system SHALL persist all pricing offers returned by the Decision Platform (Pricing Engine) against the application.

#### Scenario: All pricing offers persisted
- **WHEN** pricing offers are received from the Decision Platform
- **THEN** the system SHALL persist each offer with: `pricing_offer_id`, `approved_amount`, `interest_rate`, `apr`, `term_months`, `monthly_repayment`, `total_repayable`, `offer_expiry_date`, `pricing_model_ref`, `bureau_snapshot_ref`, and `offer_status`

#### Scenario: Pricing offers replaced on re-pricing
- **WHEN** a re-pricing event occurs due to offer expiry
- **THEN** the system SHALL mark previous pricing offers as `SUPERSEDED`
- **THEN** the system SHALL persist the new set of pricing offers as `ACTIVE`

---

### Requirement: Offer Selection Persistence
The system SHALL persist the applicant's selected pricing offer.

#### Scenario: Selected offer recorded
- **WHEN** an applicant selects a pricing offer
- **THEN** the system SHALL persist `selected_pricing_offer_id` and `offer_selected_timestamp` against the application

#### Scenario: Selected offer retained through referred path
- **WHEN** an application is referred to manual underwriting
- **THEN** the `selected_pricing_offer_id` SHALL remain unchanged throughout the underwriting review

---

### Requirement: Consent Record Persistence
The system SHALL persist explicit consent records for hard pull and offer acceptance.

#### Scenario: Hard pull consent persisted
- **WHEN** an applicant grants hard pull consent
- **THEN** the system SHALL persist a consent record with `consent_type: HARD_PULL`, `consent_given_at`, `consent_channel`, and `applicant_reference`

#### Scenario: Offer acceptance consent persisted
- **WHEN** an applicant accepts the selected offer terms
- **THEN** the system SHALL persist a consent record with `consent_type: OFFER_ACCEPTANCE`, `consent_given_at`, `consent_channel`, and `selected_pricing_offer_id`

---

### Requirement: Campaign Offer Reference Persistence
The system SHALL persist the campaign offer reference from the Offer Management Platform during ITA intake so it is available for the pricing request assembly.

#### Scenario: Campaign offer reference stored at intake
- **WHEN** an ITA journey application is created
- **THEN** the system SHALL persist `campaign_offer_id` and `campaign_offer_terms` from the Offer Management Platform against the application record

#### Scenario: No campaign offer reference for direct journey
- **WHEN** a direct journey application is created
- **THEN** the `campaign_offer_id` field SHALL be null
