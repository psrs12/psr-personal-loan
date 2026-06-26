## ADDED Requirements

### Requirement: Validate invitation identifier
The system SHALL validate the invitation identifier by calling the Enterprise Offer Management Platform. Validation SHALL confirm the invitation exists, is active, and has not expired.

#### Scenario: Valid invitation accepted
- **WHEN** a prospect submits a valid, active, non-expired invitation identifier
- **THEN** the system SHALL proceed to retrieve the associated offer

#### Scenario: Invitation not found
- **WHEN** the submitted invitation identifier does not exist in the Offer Management Platform
- **THEN** the system SHALL reject the request and return error INVITATION_NOT_FOUND

#### Scenario: Invitation expired
- **WHEN** the submitted invitation identifier has passed its expiration date
- **THEN** the system SHALL reject the request and return error INVITATION_EXPIRED

#### Scenario: Invitation inactive
- **WHEN** the submitted invitation identifier exists but is not in an active state
- **THEN** the system SHALL reject the request and return error INVITATION_NOT_FOUND

---

### Requirement: Retrieve offer details
The system SHALL retrieve offer details from the Enterprise Offer Management Platform using the validated invitation. Offer details SHALL include offer identifier, customer reference identifier, loan amount, APR, term in months, and expiration date.

#### Scenario: Offer successfully retrieved
- **WHEN** the invitation is valid and the associated offer is active
- **THEN** the system SHALL retrieve and store offer details including customerReferenceId

#### Scenario: Offer unavailable
- **WHEN** the Offer Management Platform returns a failure response
- **THEN** the system SHALL return error OFFER_UNAVAILABLE and SHALL NOT create an intake context

#### Scenario: Offer expired
- **WHEN** the associated offer has passed its expiration date
- **THEN** the system SHALL return error OFFER_EXPIRED and SHALL NOT create an intake context

---

### Requirement: Retrieve customer prefill data
The system SHALL retrieve customer information from the Customer Profile Platform using the customerReferenceId from the offer. Prefill data is limited to first name, last name, and address only. Phone and email SHALL NOT be retrieved or returned as prefill.

#### Scenario: Customer information retrieved successfully
- **WHEN** the Customer Profile Platform returns customer information
- **THEN** the system SHALL include first name, last name, and address in the prefill response

#### Scenario: Customer lookup fails
- **WHEN** the Customer Profile Platform returns a failure or the customer is not found
- **THEN** the system SHALL create the intake context with prefillStatus PARTIAL
- **THEN** the system SHALL return the offer information without customer prefill data
- **THEN** the system SHALL NOT block intake processing due to customer lookup failure

---

### Requirement: Create InvitationSession
The system SHALL create and persist an InvitationSession record upon receiving a valid invitation initialize request. The session SHALL expire 30 minutes after creation if no application has been created.

#### Scenario: Session created on valid request
- **WHEN** invitation validation succeeds
- **THEN** the system SHALL create an InvitationSession with status VALIDATED and an expiration timestamp 30 minutes from creation

#### Scenario: Session reaches COMPLETED status
- **WHEN** offer and customer retrieval succeed and intake context is created
- **THEN** the system SHALL update the InvitationSession status to COMPLETED

#### Scenario: Session reaches FAILED status
- **WHEN** offer retrieval fails
- **THEN** the system SHALL update the InvitationSession status to FAILED

---

### Requirement: Create ApplicationIntakeContext
The system SHALL create and persist an ApplicationIntakeContext after successful invitation processing. The context SHALL record the intake source, invitation identifier, offer identifier, customer reference identifier, and prefill status.

#### Scenario: Intake context created with complete prefill
- **WHEN** both offer retrieval and customer lookup succeed
- **THEN** the system SHALL create an ApplicationIntakeContext with prefillStatus COMPLETE

#### Scenario: Intake context created with partial prefill
- **WHEN** offer retrieval succeeds but customer lookup fails
- **THEN** the system SHALL create an ApplicationIntakeContext with prefillStatus PARTIAL

---

### Requirement: Return prefill response
The system SHALL return a prefill response containing the intake context identifier, session identifier, prefill status, offer details, and available customer information (name and address only).

#### Scenario: Complete prefill response
- **WHEN** intake processing completes successfully with full customer data
- **THEN** the response SHALL contain intakeId, sessionId, prefillStatus COMPLETE, offer object, and customer object with firstName, lastName, and address

#### Scenario: Partial prefill response
- **WHEN** intake processing completes but customer lookup failed
- **THEN** the response SHALL contain intakeId, sessionId, prefillStatus PARTIAL, offer object, and no customer object

---

### Requirement: Duplicate invitation processing prevention
The system SHALL prevent intake initialization for an invitation that already has an active application. An active application is one in status CREATED, IN_PROGRESS, READY_FOR_SUBMISSION, SUBMITTED, or PROCESSING.

#### Scenario: Active application exists for invitation
- **WHEN** a prospect attempts to initialize an invitation that already has an active application
- **THEN** the system SHALL reject the request with error DUPLICATE_APPLICATION

#### Scenario: Terminal application allows reuse
- **WHEN** a prospect initializes an invitation whose prior application has status CANCELLED or EXPIRED
- **THEN** the system SHALL allow intake to proceed and create a new InvitationSession and ApplicationIntakeContext

---

### Requirement: External system resilience
The system SHALL handle external system failures gracefully using circuit breaker and retry patterns for calls to Offer Management Platform and Customer Profile Platform.

#### Scenario: Offer Management Platform timeout
- **WHEN** the Offer Management Platform does not respond within the configured timeout
- **THEN** the system SHALL return error OFFER_UNAVAILABLE with HTTP 503

#### Scenario: Customer Profile Platform timeout
- **WHEN** the Customer Profile Platform does not respond within the configured timeout
- **THEN** the system SHALL treat the failure as a partial prefill and continue processing
