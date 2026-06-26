## ADDED Requirements

### Requirement: Credit assessment initiated on application creation
The system SHALL publish a `CreditAssessmentRequested` event immediately after `ApplicationCreated`. The event SHALL include the bureaus to pull and the freshness policy as configured for the Personal Loan product.

#### Scenario: Credit assessment requested after ITA application creation
- **WHEN** an ITA application is successfully created
- **THEN** the system SHALL publish `CreditAssessmentRequested` with configured bureaus and freshness window
- **THEN** application status SHALL transition to `CREDIT_ASSESSMENT_INITIATED`

#### Scenario: Credit assessment requested after DIRECT application creation
- **WHEN** a DIRECT application is successfully created
- **THEN** the system SHALL publish `CreditAssessmentRequested` with configured bureaus and freshness window
- **THEN** application status SHALL transition to `CREDIT_ASSESSMENT_INITIATED`

### Requirement: Bureau response tracking
The system SHALL record each `CreditReportRetrieved` event in the `credit_bureau_response` table, capturing bureau name, reportId, dataSource (LIVE or CACHED), and pulledAt timestamp.

#### Scenario: Primary bureau response received
- **WHEN** a `CreditReportRetrieved` event arrives for the primary bureau
- **THEN** the system SHALL record the response in `credit_bureau_response`
- **THEN** application status SHALL transition to `CREDIT_ASSESSMENT_IN_PROGRESS`

#### Scenario: Secondary bureau response received
- **WHEN** a `CreditReportRetrieved` event arrives for a secondary bureau
- **THEN** the system SHALL record the response in `credit_bureau_response`
- **THEN** application status SHALL remain `CREDIT_ASSESSMENT_IN_PROGRESS`

### Requirement: Primary bureau mandatory gate
The system SHALL NOT proceed to the decision engine if the primary bureau's report has not been received. If the primary bureau report is absent when the timeout window closes, the application SHALL transition to `CREDIT_ASSESSMENT_FAILED`.

#### Scenario: Primary bureau not received within timeout
- **WHEN** the configured timeout window elapses
- **AND** the primary bureau report has not been received
- **THEN** application status SHALL transition to `CREDIT_ASSESSMENT_FAILED`
- **THEN** no decision engine call SHALL be made
- **THEN** operations team SHALL be alerted

#### Scenario: Primary bureau received, secondary missing at timeout
- **WHEN** the configured timeout window elapses
- **AND** the primary bureau report has been received
- **AND** one or more secondary bureau reports are missing
- **THEN** the system SHALL proceed to the decision engine with available data
- **THEN** missing bureaus SHALL be flagged in the `CreditAssessmentContext`

### Requirement: Fan-in window with configurable timeout
The system SHALL wait for all requested bureau reports up to a configurable timeout (default 5 minutes). If all reports arrive before the timeout, processing SHALL begin immediately without waiting for the timeout to expire.

#### Scenario: All bureaus received before timeout
- **WHEN** all three bureau reports are received before the timeout window elapses
- **THEN** the system SHALL immediately proceed to the decision engine
- **THEN** the system SHALL NOT wait for the timeout to expire

#### Scenario: Timeout elapses with primary present
- **WHEN** the timeout window elapses
- **AND** the primary bureau report is present
- **THEN** the system SHALL proceed to the decision engine with whatever bureau data is available
