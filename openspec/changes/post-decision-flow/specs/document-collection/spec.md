## ADDED Requirements

### Requirement: Document Requirements Reception
The system SHALL consume the `FinalDecisionDocumentsRequired` domain event published by pricing-orchestration-service and store the document requirements for the application.

#### Scenario: Document requirements received from decision outcome
- **WHEN** a `FinalDecisionDocumentsRequired` event is received for an application
- **THEN** document-service SHALL extract the list of required document type codes from the event payload
- **THEN** document-service SHALL apply the ACL mapping to translate each Decision Engine code to a domain `DocumentType`
- **THEN** document-service SHALL persist a `DocumentRequirement` record for each required document containing: `applicationId`, `documentType`, `count`, `description`, `status: PENDING`
- **THEN** document-service SHALL call application-management-service to transition the application state to `DOCUMENTS_REQUIRED`

#### Scenario: Unknown document type code received
- **WHEN** a `FinalDecisionDocumentsRequired` event contains a document type code not present in the ACL mapping
- **THEN** document-service SHALL NOT persist any requirements for that application
- **THEN** document-service SHALL send the event to the dead-letter queue
- **THEN** document-service SHALL emit an `UnknownDocumentTypeAlert` for operational monitoring

---

### Requirement: Document Requirements API
The system SHALL expose an API for retrieving the document requirements for an application.

#### Scenario: Requirements retrieved by applicant
- **WHEN** a GET request is received at `/applications/{applicationId}/documents/requirements`
- **THEN** the system SHALL return the list of `DocumentRequirement` records for that application
- **THEN** each record SHALL include: `documentType`, `count`, `description`, `status` (PENDING, UPLOADED, REJECTED, COMPLETED)

#### Scenario: No requirements found
- **WHEN** a GET request is received for an application with no document requirements
- **THEN** the system SHALL return HTTP 404

---

### Requirement: Document Upload
The system SHALL accept document uploads from the applicant and track upload status per requirement.

#### Scenario: Document uploaded successfully
- **WHEN** a POST request is received at `/applications/{applicationId}/documents/upload` with a valid document file and `documentType`
- **THEN** the system SHALL store the document in the configured storage (S3)
- **THEN** the system SHALL create a `DocumentRecord` with `status: UPLOADED` and a storage reference
- **THEN** the system SHALL update the corresponding `DocumentRequirement` status to `UPLOADED`
- **THEN** the system SHALL publish a `DocumentUploaded` event

#### Scenario: Upload rejected — wrong document type
- **WHEN** a POST request is received with a `documentType` not in the application's requirements
- **THEN** the system SHALL return HTTP 422 with error code `DOCUMENT_TYPE_NOT_REQUIRED`

#### Scenario: Upload rejected — count already satisfied
- **WHEN** a POST request is received for a `documentType` where the required count is already fulfilled
- **THEN** the system SHALL return HTTP 422 with error code `DOCUMENT_COUNT_SATISFIED`

---

### Requirement: Document Lifecycle Tracking
The system SHALL track the lifecycle state of each uploaded document through virus scanning and review.

#### Scenario: Document passes virus scan
- **WHEN** the virus scanner confirms a document is clean
- **THEN** the system SHALL update the `DocumentRecord` status to `VERIFIED`
- **THEN** the system SHALL evaluate whether all requirements for the application are satisfied

#### Scenario: Document fails virus scan
- **WHEN** the virus scanner rejects a document
- **THEN** the system SHALL update the `DocumentRecord` status to `REJECTED`
- **THEN** the system SHALL update the `DocumentRequirement` status to `REJECTED`
- **THEN** the system SHALL publish a `DocumentRejected` event
- **THEN** the applicant SHALL be permitted to re-upload for that requirement

#### Scenario: All document requirements satisfied
- **WHEN** all `DocumentRequirement` records for an application reach `COMPLETED` status
- **THEN** the system SHALL publish a `DocumentsCompleted` event
- **THEN** the system SHALL call application-management-service to transition the application state to `UNDERWRITING`

---

### Requirement: ACL Document Type Mapping
The system SHALL maintain an Anti-Corruption Layer that maps Decision Engine document type codes to the personal loan platform's `DocumentType` domain model.

#### Scenario: Known code is mapped
- **WHEN** the ACL receives a Decision Engine code (e.g., `BANK_STMT_3M`)
- **THEN** the system SHALL return the corresponding domain `DocumentType` with count and description (e.g., `BANK_STATEMENT`, count: 3, "Last 3 months of bank statements")

#### Scenario: Mapping is versioned
- **WHEN** a new version of the shared event contract is published
- **THEN** the ACL SHALL be updated to include new codes before the new contract version is activated
- **THEN** existing in-flight applications SHALL continue to use the mapping version active at the time their requirements were stored
