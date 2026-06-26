## MODIFIED Requirements

### Requirement: Credit Evaluation Orchestration Supports Two Inquiry Types
The Credit Evaluation Orchestration capability SHALL support two distinct credit bureau inquiry types: soft pull and hard pull. Each SHALL be initiated independently based on domain events and SHALL produce independent outcomes.

#### Scenario: Soft pull initiated on ApplicationCreated
- **WHEN** an `ApplicationCreated` event is received
- **THEN** the system SHALL initiate a soft pull inquiry with the Credit Management Platform
- **THEN** the soft pull SHALL NOT impact the applicant's credit score
- **THEN** the system SHALL receive and persist the `credit_report_reference_id` in the application management database
- **THEN** a `SoftPullCompleted` event SHALL be published on success

#### Scenario: Hard pull initiated on ConsentCaptured
- **WHEN** a `ConsentCaptured` event is received and hard pull consent is confirmed
- **THEN** the system SHALL initiate a hard pull inquiry with the Credit Management Platform
- **THEN** the hard pull MAY impact the applicant's credit score
- **THEN** the system SHALL receive and persist the updated `credit_report_reference_id` for the hard pull
- **THEN** a `HardPullCompleted` event SHALL be published on success

#### Scenario: Soft pull failure handling
- **WHEN** the Credit Management Platform returns an error for a soft pull request
- **THEN** the system SHALL publish a `SoftPullFailed` event
- **THEN** the application SHALL transition to `DECLINED`

#### Scenario: Hard pull failure handling
- **WHEN** the Credit Management Platform returns an error for a hard pull request
- **THEN** the system SHALL publish a `HardPullFailed` event
- **THEN** the application SHALL transition to `DECLINED`

#### Scenario: Hard pull not initiated without consent
- **WHEN** no valid hard pull consent record exists for the application
- **THEN** the system SHALL NOT initiate a hard pull
