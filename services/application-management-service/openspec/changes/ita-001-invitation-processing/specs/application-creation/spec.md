## ADDED Requirements

### Requirement: Verify SSN before application creation
The system SHALL verify the applicant's SSN via an external SSN Verification Service. SSN verification is a prerequisite for application creation. The verification result is returned as a short-lived token that must be submitted with the application creation request. SSN SHALL NOT be logged at any point.

#### Scenario: SSN verified successfully
- **WHEN** a prospect submits their SSN for verification
- **THEN** the system SHALL call the external SSN Verification Service
- **THEN** the system SHALL return a verificationToken and verified status

#### Scenario: SSN verification fails
- **WHEN** the external SSN Verification Service indicates the SSN is invalid
- **THEN** the system SHALL return error SSN_VERIFICATION_FAILED
- **THEN** no application SHALL be created

#### Scenario: SSN verification token missing on application submission
- **WHEN** a prospect submits POST /applications without a valid ssnVerificationToken
- **THEN** the system SHALL reject the request with error SSN_VERIFICATION_TOKEN_INVALID

---

### Requirement: Create application from ITA intake
The system SHALL create a Personal Loan application for prospects who completed the ITA intake flow. The request SHALL include the intakeId from invitation processing, a valid ssnVerificationToken, and all applicant and loan request information.

#### Scenario: Application created from ITA intake
- **WHEN** a prospect submits a valid intakeId, valid ssnVerificationToken, and complete applicant and loan request information
- **THEN** the system SHALL create an Application with applicationSource INVITATION and status CREATED
- **THEN** the system SHALL return an applicationId and status

#### Scenario: Invalid intakeId
- **WHEN** the submitted intakeId does not exist
- **THEN** the system SHALL reject the request with error INTAKE_NOT_FOUND

#### Scenario: Expired session blocks application creation
- **WHEN** the InvitationSession associated with the intakeId has expired (30-minute window elapsed)
- **THEN** the system SHALL reject the request with error INTAKE_EXPIRED

---

### Requirement: Create application without invitation (DIRECT)
The system SHALL create a Personal Loan application for prospects who arrive without an invitation. No intakeId is required. The prospect enters all information including name and address. applicationSource is set to DIRECT.

#### Scenario: Application created for DIRECT prospect
- **WHEN** a prospect submits POST /applications without an intakeId and with a valid ssnVerificationToken and complete applicant information
- **THEN** the system SHALL create an Application with applicationSource DIRECT and status CREATED
- **THEN** no ApplicationIntakeContext or ApplicationOffer SHALL be created

---

### Requirement: Persist applicant information
The system SHALL persist applicant information as provided by the applicant on the application form. Prefill fields (first name, last name, address) may be confirmed or modified by the applicant. Phone and email are always applicant-entered and SHALL be persisted as provided.

#### Scenario: Applicant information persisted
- **WHEN** an application is created
- **THEN** the system SHALL persist first name, last name, date of birth, address, phone, email, employer name, and annual income as provided in the request

---

### Requirement: Persist offer snapshot at application creation time
For INVITATION source applications, the system SHALL snapshot and persist offer terms at the time of application creation as an ApplicationOffer record. The ApplicationOffer SHALL include offer identifier, customerReferenceId, loan amount, APR, term in months, and expiration date. Customer details (name, address, phone, email) SHALL NOT be stored in the ApplicationOffer.

#### Scenario: ApplicationOffer persisted for ITA application
- **WHEN** an application is created from an INVITATION intake context
- **THEN** the system SHALL create an ApplicationOffer record linked to the application containing offer terms and customerReferenceId

#### Scenario: No ApplicationOffer for DIRECT application
- **WHEN** an application is created from a DIRECT intake context
- **THEN** the system SHALL NOT create an ApplicationOffer record

---

### Requirement: Persist loan request
The system SHALL persist the applicant's loan request details including requested amount, term in months, and loan purpose.

#### Scenario: Loan request persisted
- **WHEN** an application is created
- **THEN** the system SHALL persist the loan request details linked to the application

---

### Requirement: Publish ApplicationCreated event
The system SHALL publish an ApplicationCreated domain event when an application is successfully created.

#### Scenario: Event published on successful creation
- **WHEN** an application is created successfully
- **THEN** the system SHALL publish an ApplicationCreated event containing applicationId, applicationSource, and createdTimestamp

---

### Requirement: Generate audit record on application creation
The system SHALL create an audit record for every application creation event.

#### Scenario: Audit record created
- **WHEN** an application is created
- **THEN** the system SHALL persist an application_audit record with event type APPLICATION_CREATED, applicationId, intakeId, and timestamp
