## ADDED Requirements

### Requirement: Pre-Screening AML Gate (Gate 1)
The system SHALL perform an AML/sanctions check via the enterprise Fraud/AML Platform when an `ApplicationSubmitted` event is received, before the soft pull is initiated. The application SHALL NOT proceed to soft pull until the screening passes.

#### Scenario: AML pre-screening passes
- **WHEN** an `ApplicationSubmitted` event is received
- **THEN** compliance-orchestration-service SHALL submit an AML screening request to the Fraud/AML Platform with `applicationId`, `applicantName`, `dateOfBirth`, and `addressDetails`
- **THEN** on a PASS result, the system SHALL publish an `AmlScreeningPassed` event
- **THEN** pricing-orchestration-service SHALL initiate the soft pull on receipt of `AmlScreeningPassed`

#### Scenario: AML pre-screening fails
- **WHEN** the Fraud/AML Platform returns a FAIL result for Gate 1
- **THEN** the system SHALL publish an `AmlScreeningFailed` event containing `applicationId`, `screeningType: PRE_SCREENING`, `failReason`, and `correlationId`
- **THEN** the system SHALL call application-management-service to place the application in `COMPLIANCE_HOLD` state
- **THEN** the system SHALL publish an `AmlHoldPlaced` event
- **THEN** pricing-orchestration-service SHALL NOT initiate the soft pull

#### Scenario: Fraud/AML Platform is unavailable at Gate 1
- **WHEN** the Fraud/AML Platform returns an error or times out during Gate 1 screening
- **THEN** the system SHALL NOT proceed to soft pull
- **THEN** the system SHALL place the application in `COMPLIANCE_HOLD`
- **THEN** the system SHALL emit an `AmlPlatformUnavailableAlert` for operational monitoring

#### Scenario: Duplicate ApplicationSubmitted event is idempotent
- **WHEN** an `ApplicationSubmitted` event is received for an application that already has a completed Gate 1 screening record
- **THEN** the system SHALL NOT submit a duplicate AML screening request
- **THEN** the event SHALL be logged and discarded

---

### Requirement: Pre-Funding AML Gate (Gate 5)
The system SHALL perform an AML/sanctions re-check via the enterprise Fraud/AML Platform when a `FundingRequested` event is received. Funding SHALL NOT proceed until the re-check passes.

#### Scenario: Pre-funding AML re-check passes
- **WHEN** a `FundingRequested` event is received for an application in `OFFER_ACCEPTED` state
- **THEN** compliance-orchestration-service SHALL submit an AML re-check request to the Fraud/AML Platform with `applicationId` and `applicantReference`
- **THEN** on a PASS result, the system SHALL publish a `PreFundingCompliancePassed` event
- **THEN** the funding-orchestration capability SHALL proceed with the funding request on receipt of `PreFundingCompliancePassed`

#### Scenario: Pre-funding AML re-check fails
- **WHEN** the Fraud/AML Platform returns a FAIL result for Gate 5
- **THEN** the system SHALL publish a `PreFundingComplianceHeld` event containing `applicationId`, `screeningType: PRE_FUNDING`, `failReason`, and `correlationId`
- **THEN** the system SHALL call application-management-service to transition the application to `COMPLIANCE_HOLD` state
- **THEN** the funding request SHALL NOT be submitted

#### Scenario: Compliance hold is released after manual review
- **WHEN** an authorised operator releases the compliance hold via `POST /applications/{applicationId}/compliance-hold/release`
- **THEN** the system SHALL transition the application state from `COMPLIANCE_HOLD` to `FUNDING_PENDING`
- **THEN** the funding request process SHALL resume

#### Scenario: Compliance hold is escalated to decline
- **WHEN** an authorised operator escalates the compliance hold via `POST /applications/{applicationId}/compliance-hold/escalate`
- **THEN** the system SHALL transition the application state to `DECLINED`
- **THEN** the adverse-action gate SHALL be triggered

---

### Requirement: AmlScreeningPassed Event
The system SHALL publish an `AmlScreeningPassed` event after a successful Gate 1 screening.

#### Scenario: Event published with required fields
- **WHEN** Gate 1 AML screening passes
- **THEN** the system SHALL publish `AmlScreeningPassed` containing: `applicationId`, `screeningType: PRE_SCREENING`, `timestamp`, `correlationId`
- **THEN** the event SHALL be published to the `compliance.aml-screening-passed` Kafka topic

---

### Requirement: AmlHoldPlaced Event
The system SHALL publish an `AmlHoldPlaced` event whenever an application is placed in compliance hold.

#### Scenario: Event published with required fields
- **WHEN** an application is placed in `COMPLIANCE_HOLD` by Gate 1 or Gate 5
- **THEN** the system SHALL publish `AmlHoldPlaced` containing: `applicationId`, `screeningType` (PRE_SCREENING or PRE_FUNDING), `timestamp`, `correlationId`
- **THEN** the event SHALL be published to the `compliance.aml-hold-placed` Kafka topic
