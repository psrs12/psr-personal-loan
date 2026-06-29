## ADDED Requirements

### Requirement: Pricing Orchestration Capability
The system SHALL provide a Pricing Orchestration capability that coordinates the end-to-end workflow from soft pull initiation through offer presentation, consent capture, hard pull initiation, and final decision routing.

#### Scenario: Pricing orchestration triggered on application creation
- **WHEN** an `ApplicationCreated` event is published
- **THEN** the system SHALL initiate the soft pull workflow for the application

#### Scenario: Pricing orchestration owns state transitions
- **WHEN** any step in the pricing workflow completes or fails
- **THEN** the system SHALL transition the application to the appropriate state and publish the corresponding domain event

---

### Requirement: Soft Pull Initiation
The system SHALL initiate a soft pull credit bureau inquiry with the Credit Management Platform upon receiving an `ApplicationCreated` event. A soft pull SHALL NOT impact the applicant's credit score.

#### Scenario: Soft pull initiated on application creation
- **WHEN** an `ApplicationCreated` event is received
- **THEN** the system SHALL call the Credit Management Platform to initiate a soft pull
- **THEN** the application state SHALL transition to `SOFT_PULL_PENDING`
- **THEN** a `SoftPullInitiated` event SHALL be published

#### Scenario: Soft pull completes successfully
- **WHEN** the Credit Management Platform returns a successful soft pull response
- **THEN** the system SHALL persist the `credit_report_reference_id` in the application management database
- **THEN** the application state SHALL transition to `PRICING_PENDING`
- **THEN** a `SoftPullCompleted` event SHALL be published

#### Scenario: Soft pull fails
- **WHEN** the Credit Management Platform returns a failure response or is unavailable
- **THEN** the application state SHALL transition to `DECLINED`
- **THEN** a `SoftPullFailed` event SHALL be published
- **THEN** the adverse action notification workflow SHALL be triggered

---

### Requirement: Pricing Request Assembly
The system SHALL assemble a pricing request from data held in the application management database and submit it to the Decision Platform acting as the Pricing Engine.

#### Scenario: Pricing request assembled for ITA journey
- **WHEN** a `SoftPullCompleted` event is received and the application has a campaign offer reference
- **THEN** the system SHALL assemble a pricing request containing: application details, applicant financials, `credit_report_reference_id`, and the campaign offer reference
- **THEN** the system SHALL submit the pricing request to the Decision Platform

#### Scenario: Pricing request assembled for direct journey
- **WHEN** a `SoftPullCompleted` event is received and the application has no campaign offer reference
- **THEN** the system SHALL assemble a pricing request containing: application details, applicant financials, `credit_report_reference_id`, and no campaign offer reference
- **THEN** the system SHALL submit the pricing request to the Decision Platform

#### Scenario: Decision Platform retrieves bureau data independently
- **WHEN** the pricing request is submitted
- **THEN** the Decision Platform SHALL use the `credit_report_reference_id` to retrieve full bureau data from the Credit Management Platform
- **THEN** the platform SHALL NOT send raw bureau data in the pricing request payload

---

### Requirement: Pricing Offers Receipt and Persistence
The system SHALL receive the list of pricing offers from the Decision Platform, persist all offers in the application management database, and make them available for applicant presentation.

#### Scenario: Pricing offers received successfully
- **WHEN** the Decision Platform returns a list of one or more pricing offers
- **THEN** the system SHALL persist all offers with their attributes including `pricing_offer_id`, `approved_amount`, `interest_rate`, `apr`, `term_months`, `monthly_repayment`, `total_repayable`, `offer_expiry_date`, and `pricing_model_ref`
- **THEN** the application state SHALL transition to `OFFER_PENDING`
- **THEN** a `PricingOffersReceived` event SHALL be published

#### Scenario: Number of offers is engine-controlled
- **WHEN** the Decision Platform returns a pricing response
- **THEN** the system SHALL accept and persist any number of offers returned
- **THEN** the system SHALL NOT enforce a minimum or maximum offer count

#### Scenario: Pricing engine returns decline
- **WHEN** the Decision Platform returns a decline response instead of offers
- **THEN** the application state SHALL transition to `DECLINED`
- **THEN** a `PricingDeclined` event SHALL be published
- **THEN** the adverse action notification workflow SHALL be triggered

---

### Requirement: Offer Expiry Validation
The system SHALL validate offer expiry dates when offers are retrieved for applicant presentation. Expired offers SHALL trigger a re-pricing workflow.

#### Scenario: Valid offers presented to applicant
- **WHEN** an applicant requests to view available offers
- **THEN** the system SHALL validate the `offer_expiry_date` of each persisted offer against the current date
- **THEN** the system SHALL present only non-expired offers to the applicant

#### Scenario: All offers have expired — re-pricing triggered
- **WHEN** an applicant requests to view available offers and all persisted offers have expired
- **THEN** the system SHALL validate the application has not expired
- **THEN** the system SHALL initiate a new soft pull
- **THEN** the system SHALL submit a new pricing request after soft pull completion
- **THEN** new pricing offers SHALL replace the previously persisted offers

#### Scenario: Application expired during offer selection
- **WHEN** an applicant requests to view available offers and the application has passed its configurable expiry threshold
- **THEN** the application state SHALL transition to `EXPIRED`
- **THEN** no further processing SHALL occur

---

### Requirement: Offer Selection and Consent Capture
The system SHALL allow the applicant to select one pricing offer and SHALL capture explicit consent for a hard pull credit inquiry and acceptance of the selected offer terms.

#### Scenario: Applicant selects an offer
- **WHEN** an applicant submits a selected `pricing_offer_id`
- **THEN** the system SHALL validate the selected offer exists and has not expired
- **THEN** the system SHALL persist the `selected_offer_id` and `offer_selected_timestamp`

#### Scenario: Hard pull consent captured
- **WHEN** an applicant submits hard pull consent
- **THEN** the system SHALL persist the consent record with `consent_type: HARD_PULL`, `consent_given_at`, and `consent_channel`
- **THEN** the system SHALL persist offer terms acceptance with `consent_type: OFFER_ACCEPTANCE`
- **THEN** the application state SHALL transition to `CONSENT_CAPTURED`
- **THEN** a `ConsentCaptured` event SHALL be published

#### Scenario: Consent not given — hard pull not initiated
- **WHEN** an applicant does not provide hard pull consent
- **THEN** the system SHALL NOT initiate a hard pull
- **THEN** the application SHALL remain in `OFFER_PENDING` state

---

### Requirement: Hard Pull Initiation
The system SHALL initiate a hard pull credit bureau inquiry with the Credit Management Platform after consent is captured. A hard pull MAY impact the applicant's credit score.

#### Scenario: Hard pull initiated after consent
- **WHEN** a `ConsentCaptured` event is received
- **THEN** the system SHALL call the Credit Management Platform to initiate a hard pull
- **THEN** the application state SHALL transition to `HARD_PULL_PENDING`
- **THEN** a `HardPullInitiated` event SHALL be published

#### Scenario: Hard pull completes successfully
- **WHEN** the Credit Management Platform returns a successful hard pull response
- **THEN** the system SHALL persist the updated `credit_report_reference_id` for the hard pull
- **THEN** the application state SHALL transition to `DECISION_PENDING`
- **THEN** a `HardPullCompleted` event SHALL be published

#### Scenario: Hard pull fails
- **WHEN** the Credit Management Platform returns a failure response or is unavailable for a hard pull
- **THEN** the application state SHALL transition to `DECLINED`
- **THEN** a `HardPullFailed` event SHALL be published
- **THEN** the adverse action notification workflow SHALL be triggered

---

### Requirement: Final Decision Routing
The system SHALL submit a final decision request to the Decision Platform after hard pull completion and route the application based on the decision outcome.

#### Scenario: Final decision request submitted
- **WHEN** a `HardPullCompleted` event is received
- **THEN** the system SHALL submit a final decision request to the Decision Platform including `application_id`, `selected_offer_id`, and `hard_pull_credit_report_reference_id`

#### Scenario: Decision Platform returns approved
- **WHEN** the Decision Platform returns an approved decision
- **THEN** the application state SHALL transition to `APPROVED`
- **THEN** the selected offer SHALL be confirmed
- **THEN** a `FinalDecisionApproved` event SHALL be published
- **THEN** the document collection workflow SHALL be triggered

#### Scenario: Decision Platform returns declined
- **WHEN** the Decision Platform returns a declined decision
- **THEN** the application state SHALL transition to `DECLINED`
- **THEN** a `FinalDecisionDeclined` event SHALL be published
- **THEN** the adverse action notification workflow SHALL be triggered

#### Scenario: Decision Platform returns referred
- **WHEN** the Decision Platform returns a referred decision
- **THEN** the application state SHALL transition to `REFERRED`
- **THEN** the applicant's selected offer SHALL be retained without modification
- **THEN** no new hard pull SHALL be initiated during manual review
- **THEN** a `FinalDecisionReferred` event SHALL be published
- **THEN** the underwriting workflow SHALL be triggered

---

### Requirement: Referred Application — Manual Underwriting Path
The system SHALL route referred applications to manual underwriting. The underwriter MAY request additional documentation. The selected offer SHALL remain unchanged throughout the underwriting process.

#### Scenario: Underwriter requests additional documentation
- **WHEN** an underwriter determines additional documentation is required during manual review
- **THEN** the system SHALL trigger the document collection workflow for the specified documents (e.g. bank statements)
- **THEN** the application state SHALL transition to `DOCUMENT_PENDING`

#### Scenario: Underwriting completes with approval
- **WHEN** an underwriter approves the application after manual review
- **THEN** the application state SHALL transition to `APPROVED`
- **THEN** the selected offer SHALL be confirmed unchanged
- **THEN** the standard post-approval workflow SHALL continue

#### Scenario: Underwriting completes with decline
- **WHEN** an underwriter declines the application after manual review
- **THEN** the application state SHALL transition to `DECLINED`
- **THEN** the adverse action notification workflow SHALL be triggered

---

### Requirement: Decline Flow at Every Stage
The system SHALL support application decline from any stage of the pricing orchestration workflow. All declines SHALL follow the same terminal transition and notification path.

#### Scenario: Application declined transitions to terminal state
- **WHEN** a decline occurs at any stage (soft pull, pricing, hard pull, or final decision)
- **THEN** the application state SHALL transition to `DECLINED`
- **THEN** the decline reason code SHALL be persisted
- **THEN** the adverse action notification workflow SHALL be triggered
- **THEN** no further processing SHALL occur on the application

---

### Requirement: Application Expiry Configuration
The system SHALL support configurable application expiry thresholds. Expired applications SHALL transition to a terminal `EXPIRED` state.

#### Scenario: Application expiry threshold is configurable
- **WHEN** the platform is configured
- **THEN** the application expiry threshold SHALL be configurable per product and channel

#### Scenario: Application expires during pricing workflow
- **WHEN** an application expiry check is performed and the application has exceeded its configured threshold
- **THEN** the application state SHALL transition to `EXPIRED`
- **THEN** all further processing SHALL halt
