## ADDED Requirements

### Requirement: Adverse Action Notice Generation on Declined Decision
The system SHALL generate an FCRA/ECOA-compliant adverse action notice when a `FinalDecisionDeclined` event is received. The notice SHALL be generated and submitted for delivery within the same processing cycle as the declined event.

#### Scenario: Adverse action notice generated on declined decision
- **WHEN** a `FinalDecisionDeclined` event is received containing `applicationId`, `reasonCode`, and `correlationId`
- **THEN** compliance-orchestration-service SHALL map the Decision Platform `reasonCode` to one or more FCRA/ECOA-compliant adverse action reason descriptions using the `AdverseActionReasonMapper`
- **THEN** the system SHALL create an `AdverseActionRecord` with: `noticeId` (UUID), `applicationId`, `reasonCodes` (mapped descriptions), `issuedAt` (server timestamp), `deliveryStatus: PENDING`
- **THEN** the system SHALL submit the notice to the Notification Platform for delivery to the applicant
- **THEN** on Notification Platform acceptance, `deliveryStatus` SHALL be updated to `SUBMITTED`
- **THEN** the system SHALL publish an `AdverseActionIssued` event

#### Scenario: Reason code is mapped to ECOA-compliant descriptions
- **WHEN** the Decision Platform provides a reason code (e.g., `CREDIT_RISK`, `INSUFFICIENT_INCOME`, `DEBT_TO_INCOME`)
- **THEN** the `AdverseActionReasonMapper` SHALL return one or more standardised ECOA reason descriptions (e.g., "Credit score below minimum threshold", "Insufficient income relative to requested loan amount")
- **THEN** the mapped descriptions SHALL be included verbatim in the notice

#### Scenario: Unknown reason code is handled safely
- **WHEN** the Decision Platform provides a reason code not present in the `AdverseActionReasonMapper`
- **THEN** the system SHALL use a configured default adverse action reason description
- **THEN** the system SHALL emit an `UnknownAdverseActionReasonAlert` for operational monitoring
- **THEN** the notice SHALL still be generated and submitted without delay

#### Scenario: Notification Platform is unavailable
- **WHEN** the Notification Platform returns an error or times out
- **THEN** the `AdverseActionRecord` `deliveryStatus` SHALL remain `PENDING`
- **THEN** the system SHALL retry delivery using an exponential backoff strategy (max 3 retries, 5-minute max interval)
- **THEN** if all retries are exhausted, `deliveryStatus` SHALL be set to `DELIVERY_FAILED` and an `AdverseActionDeliveryFailedAlert` SHALL be emitted

---

### Requirement: Adverse Action Timing Monitor
The system SHALL detect any declined application that does not have an associated `AdverseActionIssued` event within 24 hours and raise an operational alert.

#### Scenario: Missing adverse action notice detected
- **WHEN** a scheduled monitor runs and finds a `DECLINED` application with no `AdverseActionRecord` older than 24 hours
- **THEN** the system SHALL emit an `AdverseActionOverdueAlert` containing `applicationId` and `declinedAt`
- **THEN** the monitor SHALL NOT automatically generate the notice — it raises the alert for human review

---

### Requirement: Adverse Action Record Retrieval
The system SHALL expose an API to retrieve the adverse action record for a given application.

#### Scenario: Adverse action record retrieved by applicationId
- **WHEN** a GET request is received at `/compliance/adverse-action/{applicationId}`
- **THEN** the system SHALL return the `AdverseActionRecord` with: `noticeId`, `applicationId`, `reasonCodes`, `issuedAt`, `deliveryStatus`

#### Scenario: No adverse action record found
- **WHEN** a GET request is received for an application with no `AdverseActionRecord`
- **THEN** the system SHALL return HTTP 404 with error code `ADVERSE_ACTION_RECORD_NOT_FOUND`

---

### Requirement: AdverseActionIssued Event
The system SHALL publish an `AdverseActionIssued` event after the notice is submitted to the Notification Platform.

#### Scenario: Event published with required fields
- **WHEN** an adverse action notice is submitted for delivery
- **THEN** the system SHALL publish `AdverseActionIssued` containing: `applicationId`, `noticeId`, `issuedAt`, `correlationId`
- **THEN** the event SHALL be published to the `compliance.adverse-action-issued` Kafka topic

---

### Requirement: Adverse Action Reason Mapper
The system SHALL maintain an `AdverseActionReasonMapper` that translates Decision Platform reason codes to FCRA/ECOA-compliant adverse action reason descriptions. This mapper is the sole owner of adverse action reason wording within the platform.

#### Scenario: Mapping is applied for all known reason codes
- **WHEN** the `AdverseActionReasonMapper` receives a known Decision Platform reason code
- **THEN** it SHALL return one or more ECOA-compliant reason description strings

#### Scenario: Mapper is updated without service redeployment
- **WHEN** legal or compliance requires a change to the reason wording
- **THEN** the mapping SHALL be configurable without requiring a code change or service redeployment
