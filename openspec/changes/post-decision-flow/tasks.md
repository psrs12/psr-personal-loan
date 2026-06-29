## 1. Shared Event Contract

- [x] 1.1 Create `docs/contracts/decision-engine-events/v1/documents-required.json` with versioned schema defining event structure and document type code vocabulary
- [x] 1.2 Document the agreed document type codes (BANK_STMT_3M, PAYSLIP_2, etc.) and their domain mappings in the contract file

## 2. pricing-orchestration-service — DOCUMENTS_REQUIRED Outcome

- [x] 2.1 Add `DOCUMENTS_REQUIRED` to `FinalDecisionOutcome` enum in `FinalDecisionResponse`
- [x] 2.2 Add `documents` list (Decision Engine codes + counts) to `FinalDecisionResponse` payload
- [x] 2.3 Update `DecisionPlatformAdapter` to parse `DOCUMENTS_REQUIRED` outcome and extract document list from `FinalDecisionApiResponse`
- [x] 2.4 Add `publishFinalDecisionDocumentsRequired(UUID applicationId, List<DocumentCode> documents)` to `PricingEventPublisher` port and `KafkaPricingEventPublisher` implementation
- [x] 2.5 Add `DOCUMENTS_REQUIRED` branch to `HardPullOrchestrationService.requestFinalDecision()` — transitions state to `DOCUMENTS_REQUIRED` and publishes `FinalDecisionDocumentsRequired` event with codes forwarded verbatim
- [x] 2.6 Add unit test for `DOCUMENTS_REQUIRED` routing in `HardPullOrchestrationServiceTest`

## 3. application-management-service — New States and Login

- [x] 3.1 Add `DOCUMENTS_REQUIRED` and `OFFER_ACCEPTED` to `ApplicationStatus` enum
- [x] 3.2 Add Flyway migration `V15__add_post_decision_states.sql` — add new status values as valid enum entries
- [x] 3.3 Implement valid transition rules for `DOCUMENTS_REQUIRED → UNDERWRITING` and `OFFER_ACCEPTED → FUNDING_PENDING` in `Application.transitionTo()`
- [x] 3.4 Add `ESignCompleted` event consumer that calls `application.transitionTo(OFFER_ACCEPTED)`
- [x] 3.5 Add `DocumentsCompleted` event consumer that calls `application.transitionTo(UNDERWRITING)`
- [x] 3.6 Create `VerificationPort` interface with `verify(UUID applicationId, String last4SSN, LocalDate dateOfBirth): boolean`
- [x] 3.7 Implement `StubVerificationAdapter` that compares last4SSN against stored SSN token suffix and DOB against applicant record; guarded by `verification.stub.enabled` feature flag
- [x] 3.8 Create `ApplicantLoginUseCase` — validates applicationId, calls VerificationPort, rejects terminal states, returns JWT session token + applicationStatus
- [x] 3.9 Add `POST /applications/login` endpoint to `ApplicationController` with request body `applicationId`, `last4SSN`, `dateOfBirth`
- [x] 3.10 Add Flyway migration for any schema changes required by new states (state history table if not already present)
- [x] 3.11 Write unit tests for `ApplicantLoginUseCase` — valid credentials, not found, verification failure, terminal state rejection
- [x] 3.12 Write acceptance test for `POST /applications/login`

## 4. document-service — New Service

- [x] 4.1 Scaffold new Spring Boot service `services/document-service` with hexagonal package structure (domain, application, infrastructure, api)
- [x] 4.2 Add `pom.xml` with dependencies: Spring Boot, Spring Kafka, Spring Data JPA, Flyway, PostgreSQL, Testcontainers
- [x] 4.3 Define domain model: `DocumentRequirement` (applicationId, documentType, count, description, status), `DocumentRecord` (applicationId, documentType, storageReference, status)
- [x] 4.4 Define `DocumentType` enum with all supported types (BANK_STATEMENT, PAY_SLIP, etc.)
- [x] 4.5 Implement `DecisionEngineDocumentCodeMapper` (ACL adapter) mapping Decision Engine codes to `DocumentType` with count and description; throws `UnknownDocumentTypeException` for unmapped codes
- [x] 4.6 Create `FinalDecisionDocumentsRequiredConsumer` — consumes event, applies ACL mapping, persists `DocumentRequirement` records, calls application-management-service to transition state
- [x] 4.7 Implement `ApplicationManagementPort` and REST adapter to call `application-management-service` state update endpoint
- [x] 4.8 Create Flyway migrations for `document_requirement` and `document_record` tables
- [x] 4.9 Implement `GET /applications/{applicationId}/documents/requirements` endpoint
- [x] 4.10 Implement `POST /applications/{applicationId}/documents/upload` endpoint — validates requirement exists, count not satisfied, stores to S3, creates `DocumentRecord`
- [x] 4.11 Implement `StoragePort` and `S3StorageAdapter` for document upload
- [x] 4.12 Implement virus scan callback endpoint (or event consumer) — updates `DocumentRecord` status, evaluates completion, publishes `DocumentsCompleted` if all satisfied
- [x] 4.13 Add dead-letter queue consumer for `UnknownDocumentTypeException` — logs alert
- [x] 4.14 Write unit tests for `DecisionEngineDocumentCodeMapper` — known codes, unknown code exception
- [x] 4.15 Write unit tests for document upload validation — wrong type, count satisfied
- [ ] 4.16 Write integration tests for full event-to-requirements flow with Testcontainers

## 5. offer-acceptance-service — New Service

- [x] 5.1 Scaffold new Spring Boot service `services/offer-acceptance-service` with hexagonal package structure
- [x] 5.2 Add `pom.xml` with dependencies: Spring Boot, Spring Kafka, Spring Data JPA, Flyway, PostgreSQL, Testcontainers
- [x] 5.3 Define domain model: `OfferAcceptanceSession` (applicationId, status, declarations list), `ESignRecord` (applicationId, signedAt, ipAddress, declarationsAccepted)
- [x] 5.4 Define `Declaration` value object (declarationId, declarationType, title, content, mandatory)
- [x] 5.5 Create `FinalDecisionApprovedConsumer` — consumes event, creates `OfferAcceptanceSession` idempotently
- [x] 5.6 Create Flyway migrations for `offer_acceptance_session` and `esign_record` tables
- [x] 5.7 Implement `GET /applications/{applicationId}/declarations` endpoint
- [x] 5.8 Implement `POST /applications/{applicationId}/esign` endpoint — validates all mandatory declarations present, records e-sign, publishes `ESignCompleted`
- [x] 5.9 Implement `KafkaOfferAcceptanceEventPublisher` publishing `ESignCompleted`
- [x] 5.10 Write unit tests for `ESignUseCase` — successful sign, missing mandatory declaration, already signed
- [ ] 5.11 Write acceptance tests for declarations retrieval and e-sign submission

## 6. application-management-ui — Shell and Post-Decision MFEs

- [x] 6.1 Add applicant login page component (`/login`) — form with applicationId, last4SSN, dateOfBirth fields; calls `POST /applications/login`; stores session token; routes to application state
- [x] 6.2 Implement shell state-based routing — poll `/applications/{id}` every 3 seconds; map `applicationStatus` to micro-frontend route
- [x] 6.3 Implement `offer-acceptance-mfe` — fetches declarations from offer-acceptance-service, renders declaration list, e-sign submit button
- [x] 6.4 Implement `denial-mfe` — denial message page, adverse action information, no further actions available
- [x] 6.5 Implement `document-upload-mfe` — fetches requirements from document-service, renders dynamic upload slots per requirement type and count, file upload per slot, upload status per document
- [x] 6.6 Implement `confirmation-mfe` — post e-sign confirmation page shown when state is `OFFER_ACCEPTED`
- [x] 6.7 Add shell route table mapping all application states to the correct micro-frontend
- [x] 6.8 Add session token to all authenticated API calls from the shell and MFEs
