## ADDED Requirements

### Requirement: Credit assessment context assembled before decision engine call
The system SHALL assemble a `CreditAssessmentContext` containing the applicationId, applicantId, and bureau status entries (bureau name, reportId, isPrimary, dataSource, pulledAt, received flag) before invoking the decision engine.

#### Scenario: Context assembled with all bureaus received
- **WHEN** all requested bureau reports have been received
- **THEN** the context SHALL contain three bureau status entries, all with received=true
- **THEN** each entry SHALL include reportId, dataSource (LIVE or CACHED), and pulledAt

#### Scenario: Context assembled with missing secondary bureau
- **WHEN** one secondary bureau report was not received
- **THEN** the context SHALL contain that bureau's status entry with received=false and reportId=null
- **THEN** the decision engine SHALL use compensating rules for the missing bureau

### Requirement: Decision engine called synchronously with 5-second SLA
The system SHALL call the Personal Loan Decision Engine synchronously after assembling the credit assessment context. The call SHALL timeout at 8 seconds. On timeout or failure, the application SHALL transition to `REFERRED_FOR_REVIEW`.

#### Scenario: Decision engine returns within SLA
- **WHEN** the decision engine responds within 8 seconds
- **THEN** the system SHALL process the `DecisionEngineResult`
- **THEN** application status SHALL transition to `APPROVED`, `DECLINED`, or `REFERRED_FOR_REVIEW` based on the result

#### Scenario: Decision engine timeout
- **WHEN** the decision engine does not respond within 8 seconds
- **THEN** application status SHALL transition to `REFERRED_FOR_REVIEW`
- **THEN** the system SHALL log a timeout alert
- **THEN** no retry SHALL be attempted automatically

#### Scenario: Decision engine circuit open
- **WHEN** the decision engine circuit breaker is open
- **THEN** application status SHALL transition to `REFERRED_FOR_REVIEW`
- **THEN** the application SHALL be queued for manual review

### Requirement: Freshness metadata influences decision engine rules
The system SHALL include `dataSource` (LIVE or CACHED) and `pulledAt` timestamp for each bureau report in the context. The decision engine SHALL use this to apply confidence-weighted rules.

#### Scenario: Cached bureau data within acceptable window
- **WHEN** a bureau report has dataSource=CACHED and was pulled within the configured freshness window
- **THEN** the decision engine SHALL apply the cached data with a configured risk buffer

#### Scenario: Cached bureau data outside acceptable window
- **WHEN** a bureau report has dataSource=CACHED and was pulled outside the configured freshness window
- **THEN** the decision engine SHALL treat the report as missing and apply compensating rules

### Requirement: Decision result published as domain event
The system SHALL publish a `DecisionReached` event after receiving the decision engine result. The event SHALL include applicationId, decision (APPROVED, DECLINED, REFERRED_FOR_REVIEW), and timestamp.

#### Scenario: Approved decision published
- **WHEN** the decision engine returns APPROVED
- **THEN** application status SHALL transition to APPROVED
- **THEN** `DecisionReached` event SHALL be published with decision=APPROVED

#### Scenario: Declined decision published
- **WHEN** the decision engine returns DECLINED
- **THEN** application status SHALL transition to DECLINED
- **THEN** `DecisionReached` event SHALL be published with decision=DECLINED
