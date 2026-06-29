## 1. Project Setup

- [x] 1.1 Initialise Spring Boot 3 project with Java 21, Maven/Gradle, and required dependencies (Spring Web, Spring Data JPA, Spring Kafka, Resilience4j, Flyway, PostgreSQL driver)
- [x] 1.2 Configure application properties: datasource, Kafka, Resilience4j circuit breaker and retry, server port, and base path
- [x] 1.3 Set up package structure: domain, application, infrastructure, api layers

## 2. Database Migrations

- [x] 2.1 Create Flyway migration V1: `invitation_session` table with indexes
- [x] 2.2 Create Flyway migration V2: `offer_snapshot` table with FK to invitation_session
- [x] 2.3 Create Flyway migration V3: `application_intake_context` table with indexes
- [x] 2.4 Create Flyway migration V4: `application` table with FK to application_intake_context and indexes
- [x] 2.5 Create Flyway migration V5: `applicant` table with FK to application
- [x] 2.6 Create Flyway migration V6: `loan_request` table with FK to application
- [x] 2.7 Create Flyway migration V7: `application_offer` table with FK to application
- [x] 2.8 Create Flyway migration V8: `application_audit` table with indexes

## 3. Domain Layer — Invitation Processing

- [x] 3.1 Create `InvitationSession` aggregate with attributes, status enum (VALIDATED, COMPLETED, FAILED, EXPIRED), and business methods
- [x] 3.2 Create `ApplicationIntakeContext` aggregate with attributes, applicationSource enum (INVITATION, DIRECT, PARTNER), and prefillStatus enum (NOT_APPLICABLE, COMPLETE, PARTIAL, FAILED)
- [x] 3.3 Create `OfferDetails` value object (offerId, customerReferenceId, loanAmount, apr, termMonths, expirationDate)
- [x] 3.4 Create `CustomerPrefill` value object (firstName, lastName, address) — phone and email excluded
- [x] 3.5 Create `InvitationSessionRepository` port (interface)
- [x] 3.6 Create `ApplicationIntakeContextRepository` port (interface)
- [x] 3.7 Create `OfferManagementPort` port (interface): validateInvitation, retrieveOffer
- [x] 3.8 Create `CustomerProfilePort` port (interface): retrieveCustomer

## 4. Domain Layer — Application Creation

- [x] 4.1 Create `Application` aggregate with attributes and status enum (CREATED, IN_PROGRESS, READY_FOR_SUBMISSION, SUBMITTED, APPROVED, DECLINED, CANCELLED, EXPIRED)
- [x] 4.2 Create `Applicant` entity with all attributes: firstName, lastName, dateOfBirth, citizenship, ssn (encrypted), address, phone, email, employerName, employmentStatus, annualIncome
- [x] 4.3 Create `LoanRequest` entity (requestedAmount, termMonths, loanPurpose)
- [x] 4.4 Create `ApplicationOffer` entity (applicationOfferId, offerId, customerReferenceId, loanAmount, apr, termMonths, expirationDate, capturedTimestamp)
- [x] 4.5 Create `ApplicationRepository` port (interface)
- [x] 4.6 Create `ApplicationEventPublisher` port (interface): publishApplicationCreated
- [x] 4.7 Create `ApplicationCreatedEvent` domain event
- [x] 4.8 Create `SSNVerificationPort` port (interface): verifySSN returning verificationToken
- [x] 4.9 Create `SSNVerificationToken` value object (token, expiresAt)

## 5. Application Layer — Use Cases

- [x] 5.1 Implement `ProcessInvitationUseCase`: orchestrate validate → retrieve offer → retrieve customer → create session → create intake context → return prefill response
- [x] 5.2 Implement session expiration logic in `ProcessInvitationUseCase` (set expirationTimestamp = now + 30 minutes)
- [x] 5.3 Implement duplicate application check in `ProcessInvitationUseCase` (reject if active application exists for invitation)
- [x] 5.4 Implement customer lookup failure handling: continue with prefillStatus PARTIAL
- [x] 5.5 Implement `VerifySSNUseCase`: call SSNVerificationPort → return verificationToken on success, throw SSNVerificationFailedException on failure. SSN must not be logged at any point.
- [x] 5.6 Implement `CreateApplicationUseCase` for ITA path: validate ssnVerificationToken → validate intakeId → check session expiry → create Application + Applicant + LoanRequest → snapshot ApplicationOffer → publish event → create audit record
- [x] 5.7 Implement `CreateApplicationUseCase` for DIRECT path: validate ssnVerificationToken → create Application (source=DIRECT) + Applicant + LoanRequest → publish event → create audit record (no intakeId, no ApplicationOffer)

## 6. Infrastructure — JPA Adapters

- [x] 6.1 Create JPA entities and repositories for `invitation_session` and `offer_snapshot`
- [x] 6.2 Create JPA entities and repositories for `application_intake_context`
- [x] 6.3 Create JPA entities and repositories for `application`, `applicant`, `loan_request`, `application_offer`
- [x] 6.4 Create JPA entity and repository for `application_audit`
- [x] 6.5 Implement `InvitationSessionJpaAdapter` implementing `InvitationSessionRepository`
- [x] 6.6 Implement `ApplicationIntakeContextJpaAdapter` implementing `ApplicationIntakeContextRepository`
- [x] 6.7 Implement `ApplicationJpaAdapter` implementing `ApplicationRepository`

## 7. Infrastructure — External Adapters

- [x] 7.1 Implement `OfferManagementAdapter` with Resilience4j circuit breaker and retry: validateInvitation, retrieveOffer
- [x] 7.2 Implement `CustomerProfileAdapter` with Resilience4j circuit breaker: retrieveCustomer
- [x] 7.3 Implement `SSNVerificationAdapter` with Resilience4j circuit breaker: verifySSN. Ensure SSN is never written to logs — configure log sanitisation.
- [x] 7.4 Configure RestClient/WebClient beans for Offer Management, Customer Profile, and SSN Verification with timeout settings
- [x] 7.5 Map external API error responses to domain exceptions (INVITATION_NOT_FOUND, INVITATION_EXPIRED, OFFER_UNAVAILABLE, OFFER_EXPIRED, CUSTOMER_INFORMATION_UNAVAILABLE, SSN_VERIFICATION_FAILED)

## 8. Infrastructure — Event Publishing

- [x] 8.1 Implement `KafkaApplicationEventPublisher` implementing `ApplicationEventPublisher`
- [x] 8.2 Configure Kafka producer with topic `application-events` and serialisation settings

## 9. API Layer

- [x] 9.1 Implement `InvitationController` with `POST /api/v1/application-management/invitations/initialize`
- [x] 9.2 Create `InitializeInvitationRequest` and `InvitationPrefillResponse` DTOs
- [x] 9.3 Implement `SSNController` with `POST /api/v1/application-management/ssn/verify`. Ensure SSN is not included in any logs or request traces.
- [x] 9.4 Create `SSNVerifyRequest` and `SSNVerifyResponse` DTOs
- [x] 9.5 Implement `ApplicationController` with `POST /api/v1/application-management/applications` (intakeId optional)
- [x] 9.6 Create `CreateApplicationRequest` (intakeId nullable) and `CreateApplicationResponse` DTOs
- [x] 9.7 Implement global exception handler mapping domain exceptions to HTTP error responses with errorCode, message, correlationId
- [x] 9.8 Implement request validation (Bean Validation) for required headers and request body fields
- [x] 9.9 Configure security filter for Bearer token validation and header extraction
- [x] 9.10 Configure log sanitisation to mask SSN and other sensitive fields in all log output

## 10. Unit Tests

- [x] 10.1 Unit test `InvitationSession` aggregate — status transitions, expiration logic
- [x] 10.2 Unit test `ApplicationIntakeContext` aggregate — source and prefill status
- [x] 10.3 Unit test `Application` aggregate — status transitions
- [x] 10.4 Unit test `ProcessInvitationUseCase` — happy path, invalid invitation, expired invitation, offer failure, customer failure (partial prefill), duplicate application
- [x] 10.5 Unit test `CreateApplicationUseCase` — happy path ITA, happy path DIRECT, expired session, invalid intakeId, ApplicationOffer snapshot

## 11. Integration Tests

- [x] 11.1 Integration test `OfferManagementAdapter` with WireMock — valid response, not found, timeout, circuit breaker
- [x] 11.2 Integration test `CustomerProfileAdapter` with WireMock — valid response, not found, timeout
- [x] 11.3 Integration test JPA adapters with Testcontainers PostgreSQL — save and retrieve for all entities
- [x] 11.4 Integration test `KafkaApplicationEventPublisher` with Testcontainers Kafka — event published and readable

## 12. API / Acceptance Tests

- [x] 12.1 End-to-end test: valid ITA invitation → prefill response with offer, name, and address (no phone/email)
- [x] 12.2 End-to-end test: expired invitation → INVITATION_EXPIRED error
- [x] 12.3 End-to-end test: invitation not found → INVITATION_NOT_FOUND error
- [x] 12.4 End-to-end test: customer lookup failure → partial prefill response with offer only
- [x] 12.5 End-to-end test: duplicate active application → DUPLICATE_APPLICATION error (covered in ProcessInvitationUseCase unit test)
- [x] 12.6 End-to-end test: SSN verification success → verificationToken returned
- [x] 12.7 End-to-end test: SSN verification failure → SSN_VERIFICATION_FAILED error, no application created
- [x] 12.8 End-to-end test: create ITA application with valid token → applicationId returned, ApplicationOffer persisted, event published
- [x] 12.9 End-to-end test: create DIRECT application (no intakeId) → applicationId returned, source=DIRECT, no ApplicationOffer
- [x] 12.10 End-to-end test: create application without ssnVerificationToken → SSN_VERIFICATION_TOKEN_INVALID error
- [x] 12.11 End-to-end test: create application with expired session → INTAKE_EXPIRED error (covered in CreateApplicationUseCase unit test)
- [x] 12.12 End-to-end test: Offer Management Platform unavailable → OFFER_UNAVAILABLE 503
