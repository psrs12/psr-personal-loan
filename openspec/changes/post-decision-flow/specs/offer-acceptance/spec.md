## ADDED Requirements

### Requirement: Offer Acceptance Session Initialisation
The system SHALL initialise an offer acceptance session when a `FinalDecisionApproved` event is received, making declarations available for the applicant to review.

#### Scenario: Acceptance session created on approval
- **WHEN** a `FinalDecisionApproved` event is received for an application
- **THEN** offer-acceptance-service SHALL create an `OfferAcceptanceSession` for that application
- **THEN** the session SHALL contain the set of declarations applicable to the loan product
- **THEN** the session SHALL have status `PENDING_ESIGN`

#### Scenario: Duplicate approval event is idempotent
- **WHEN** a `FinalDecisionApproved` event is received for an application that already has an `OfferAcceptanceSession`
- **THEN** the system SHALL NOT create a duplicate session
- **THEN** the system SHALL log the duplicate and discard the event

---

### Requirement: Declarations Retrieval
The system SHALL expose an API for the applicant to retrieve the declarations they must accept before e-signing.

#### Scenario: Declarations returned for approved application
- **WHEN** a GET request is received at `/applications/{applicationId}/declarations`
- **THEN** the system SHALL return the list of declarations for that application's `OfferAcceptanceSession`
- **THEN** each declaration SHALL include: `declarationId`, `declarationType`, `title`, `content`, `mandatory`

#### Scenario: Declarations not found — application not approved
- **WHEN** a GET request is received for an application with no `OfferAcceptanceSession`
- **THEN** the system SHALL return HTTP 404 with error code `ACCEPTANCE_SESSION_NOT_FOUND`

---

### Requirement: E-Sign Capture
The system SHALL capture the applicant's e-sign acceptance of all mandatory declarations and transition the application to `OFFER_ACCEPTED`.

#### Scenario: E-sign submitted with all mandatory declarations accepted
- **WHEN** a POST request is received at `/applications/{applicationId}/esign` with all mandatory `declarationId` values included
- **THEN** the system SHALL record the e-sign with: `applicationId`, `signedAt`, `ipAddress`, `declarationsAccepted` list
- **THEN** the system SHALL update the `OfferAcceptanceSession` status to `ESIGNED`
- **THEN** the system SHALL publish an `ESignCompleted` event
- **THEN** application-management-service SHALL transition the application state to `OFFER_ACCEPTED`

#### Scenario: E-sign rejected — missing mandatory declaration
- **WHEN** a POST request is received at `/applications/{applicationId}/esign` with one or more mandatory declarations missing
- **THEN** the system SHALL return HTTP 422 with error code `MANDATORY_DECLARATION_NOT_ACCEPTED`
- **THEN** the system SHALL NOT record the e-sign or publish any event

#### Scenario: E-sign rejected — session already completed
- **WHEN** a POST request is received for an application whose `OfferAcceptanceSession` is already `ESIGNED`
- **THEN** the system SHALL return HTTP 409 with error code `ESIGN_ALREADY_COMPLETED`

---

### Requirement: ESignCompleted Event
The system SHALL publish an `ESignCompleted` domain event after successful e-sign capture.

#### Scenario: Event published with required fields
- **WHEN** an e-sign is successfully captured
- **THEN** the system SHALL publish `ESignCompleted` containing: `applicationId`, `signedAt`, `correlationId`
- **THEN** application-management-service SHALL consume this event and transition the application to `OFFER_ACCEPTED`
