## ADDED Requirements

### Requirement: FCRA Credit Pull Consent Record
The system SHALL create an immutable FCRA consent audit record before a hard pull is initiated. The hard pull SHALL NOT proceed until `FcraConsentAuditConfirmed` is returned to the caller.

#### Scenario: Consent audit record created on hard pull request
- **WHEN** `pricing-orchestration-service` calls `POST /compliance/fcra-consent` with `applicationId`, `applicantId`, `consentTimestamp`, and `selectedOfferId`
- **THEN** compliance-orchestration-service SHALL persist a `FcraConsentRecord` with: `consentId` (UUID), `applicationId`, `applicantId`, `selectedOfferId`, `consentTimestamp`, `recordedAt` (server timestamp), `creditPullType: HARD`
- **THEN** the system SHALL return `200 OK` with `consentId` and `status: CONFIRMED`
- **THEN** `pricing-orchestration-service` SHALL initiate the hard pull only after receiving this response

#### Scenario: Consent record is immutable after creation
- **WHEN** a `FcraConsentRecord` has been created for an application
- **THEN** the record SHALL NOT be modified or deleted by any service
- **THEN** any attempt to update or delete the record SHALL be rejected with HTTP 405

#### Scenario: Duplicate consent request is idempotent
- **WHEN** `POST /compliance/fcra-consent` is called for an application that already has a `FcraConsentRecord` with `creditPullType: HARD`
- **THEN** the system SHALL NOT create a duplicate record
- **THEN** the system SHALL return `200 OK` with the existing `consentId` and `status: CONFIRMED`

#### Scenario: Missing required fields are rejected
- **WHEN** `POST /compliance/fcra-consent` is received without `consentTimestamp` or `selectedOfferId`
- **THEN** the system SHALL return HTTP 422 with error code `FCRA_CONSENT_INVALID_REQUEST`
- **THEN** the hard pull SHALL NOT be initiated

---

### Requirement: FCRA Consent Record Retrieval
The system SHALL expose an API to retrieve the FCRA consent record for a given application for audit and compliance review purposes.

#### Scenario: Consent record retrieved by applicationId
- **WHEN** a GET request is received at `/compliance/fcra-consent/{applicationId}`
- **THEN** the system SHALL return the `FcraConsentRecord` with all fields: `consentId`, `applicationId`, `applicantId`, `selectedOfferId`, `consentTimestamp`, `recordedAt`, `creditPullType`

#### Scenario: No consent record found
- **WHEN** a GET request is received for an application with no `FcraConsentRecord`
- **THEN** the system SHALL return HTTP 404 with error code `FCRA_CONSENT_RECORD_NOT_FOUND`

---

### Requirement: FcraConsentAuditConfirmed Event
The system SHALL publish an `FcraConsentAuditConfirmed` event after a consent record is created, enabling event-driven consumers to react to confirmation.

#### Scenario: Event published with required fields
- **WHEN** a `FcraConsentRecord` is successfully created
- **THEN** the system SHALL publish `FcraConsentAuditConfirmed` containing: `applicationId`, `consentId`, `consentTimestamp`, `timestamp`, `correlationId`
- **THEN** the event SHALL be published to the `compliance.fcra-consent-confirmed` Kafka topic

---

### Requirement: Consent Record Retention
The system SHALL retain all FCRA consent records for a minimum of 7 years in accordance with platform data retention policy and FCRA obligations.

#### Scenario: Consent record is not purged before retention period
- **WHEN** a `FcraConsentRecord` exists for a completed or declined application
- **THEN** the record SHALL remain retrievable for a minimum of 7 years from `recordedAt`
- **THEN** no automated process SHALL delete the record before the retention period expires
