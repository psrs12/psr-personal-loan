## ADDED Requirements

### Requirement: TILA Disclosure Audit Record Creation
The system SHALL create an immutable TILA disclosure audit record when an `ESignCompleted` event is received, capturing the exact disclosure values that were presented and accepted by the applicant at the time of signing.

#### Scenario: TILA audit record created on ESignCompleted
- **WHEN** an `ESignCompleted` event is received containing `applicationId`, `signedAt`, `apr`, `totalOfPayments`, `financeCharge`, and `loanTerm`
- **THEN** compliance-orchestration-service SHALL create a `TilaDisclosureAuditRecord` with: `auditId` (UUID), `applicationId`, `apr`, `totalOfPayments`, `financeCharge`, `loanTerm`, `presentedAt` (from `signedAt`), `recordedAt` (server timestamp)
- **THEN** the record SHALL be persisted as immutable — no field may be updated after creation
- **THEN** the system SHALL publish a `TilaDisclosureAuditCreated` event

#### Scenario: Missing TILA fields in ESignCompleted event are rejected
- **WHEN** an `ESignCompleted` event is received without one or more of the required TILA fields (`apr`, `totalOfPayments`, `financeCharge`, `loanTerm`)
- **THEN** the system SHALL NOT create an audit record
- **THEN** the system SHALL route the event to the dead-letter queue
- **THEN** the system SHALL emit a `TilaDisclosureAuditFailedAlert` for operational monitoring

#### Scenario: Duplicate ESignCompleted event is idempotent
- **WHEN** an `ESignCompleted` event is received for an application that already has a `TilaDisclosureAuditRecord`
- **THEN** the system SHALL NOT create a duplicate record
- **THEN** the event SHALL be logged and discarded

#### Scenario: Audit record is immutable after creation
- **WHEN** a `TilaDisclosureAuditRecord` has been created for an application
- **THEN** no service or process SHALL modify any field on the record
- **THEN** any attempt to update the record SHALL be rejected with HTTP 405

---

### Requirement: TILA Disclosure Audit Record Retrieval
The system SHALL expose an API to retrieve the TILA disclosure audit record for a given application for regulatory examination and internal audit purposes.

#### Scenario: Audit record retrieved by applicationId
- **WHEN** a GET request is received at `/compliance/tila-disclosure/{applicationId}`
- **THEN** the system SHALL return the `TilaDisclosureAuditRecord` with all fields: `auditId`, `applicationId`, `apr`, `totalOfPayments`, `financeCharge`, `loanTerm`, `presentedAt`, `recordedAt`

#### Scenario: No audit record found
- **WHEN** a GET request is received for an application with no `TilaDisclosureAuditRecord`
- **THEN** the system SHALL return HTTP 404 with error code `TILA_AUDIT_RECORD_NOT_FOUND`

---

### Requirement: TILA Fields in ESignCompleted Event
The `ESignCompleted` event published by `offer-acceptance-service` SHALL be extended to include the TILA disclosure fields that were presented to the applicant at e-sign time.

#### Scenario: ESignCompleted event includes TILA fields
- **WHEN** an applicant completes e-sign via `POST /applications/{applicationId}/esign`
- **THEN** the `ESignCompleted` event payload SHALL include: `applicationId`, `signedAt`, `correlationId`, `apr` (percentage, 2dp), `totalOfPayments` (monetary amount), `financeCharge` (monetary amount), `loanTerm` (integer, months)
- **THEN** these values SHALL be sourced from the confirmed offer record at the time of signing and SHALL NOT be re-derived after the fact

#### Scenario: TILA fields sourced from confirmed offer at signing time
- **WHEN** the e-sign request is processed
- **THEN** `offer-acceptance-service` SHALL read `apr`, `totalOfPayments`, `financeCharge`, and `loanTerm` from the `ConfirmedOffer` record associated with the application
- **THEN** these values SHALL be included in the `ESignCompleted` event payload verbatim

---

### Requirement: TilaDisclosureAuditCreated Event
The system SHALL publish a `TilaDisclosureAuditCreated` event after the audit record is successfully persisted.

#### Scenario: Event published with required fields
- **WHEN** a `TilaDisclosureAuditRecord` is created
- **THEN** the system SHALL publish `TilaDisclosureAuditCreated` containing: `applicationId`, `auditId`, `recordedAt`, `correlationId`
- **THEN** the event SHALL be published to the `compliance.tila-disclosure-audit-created` Kafka topic

---

### Requirement: TILA Audit Record Retention
The system SHALL retain all TILA disclosure audit records for a minimum of 7 years in accordance with platform data retention policy and TILA/Regulation Z obligations.

#### Scenario: Audit record is not purged before retention period
- **WHEN** a `TilaDisclosureAuditRecord` exists for a completed application
- **THEN** the record SHALL remain retrievable for a minimum of 7 years from `recordedAt`
- **THEN** no automated process SHALL delete the record before the retention period expires

## MODIFIED Requirements

### Requirement: E-Sign Capture
The system SHALL capture the applicant's e-sign acceptance of all mandatory declarations and transition the application to `OFFER_ACCEPTED`. The e-sign request SHALL include TILA disclosure field values from the confirmed offer so that they can be included in the `ESignCompleted` event for downstream compliance audit.

#### Scenario: E-sign submitted with all mandatory declarations accepted
- **WHEN** a POST request is received at `/applications/{applicationId}/esign` with all mandatory `declarationId` values included and TILA fields present (`apr`, `totalOfPayments`, `financeCharge`, `loanTerm`)
- **THEN** the system SHALL record the e-sign with: `applicationId`, `signedAt`, `ipAddress`, `declarationsAccepted` list
- **THEN** the system SHALL update the `OfferAcceptanceSession` status to `ESIGNED`
- **THEN** the system SHALL publish an `ESignCompleted` event including TILA fields
- **THEN** application-management-service SHALL transition the application state to `OFFER_ACCEPTED`

#### Scenario: E-sign rejected — missing mandatory declaration
- **WHEN** a POST request is received at `/applications/{applicationId}/esign` with one or more mandatory declarations missing
- **THEN** the system SHALL return HTTP 422 with error code `MANDATORY_DECLARATION_NOT_ACCEPTED`
- **THEN** the system SHALL NOT record the e-sign or publish any event

#### Scenario: E-sign rejected — session already completed
- **WHEN** a POST request is received for an application whose `OfferAcceptanceSession` is already `ESIGNED`
- **THEN** the system SHALL return HTTP 409 with error code `ESIGN_ALREADY_COMPLETED`

#### Scenario: E-sign rejected — missing TILA fields
- **WHEN** a POST request is received without one or more required TILA fields (`apr`, `totalOfPayments`, `financeCharge`, `loanTerm`)
- **THEN** the system SHALL return HTTP 422 with error code `TILA_FIELDS_REQUIRED`
- **THEN** the system SHALL NOT record the e-sign or publish any event
