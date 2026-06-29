## ADDED Requirements

### Requirement: Applicant Login
The system SHALL provide an endpoint for applicants to re-enter the application journey by verifying their identity using their application ID, last 4 digits of SSN, and date of birth.

#### Scenario: Valid credentials — session token returned
- **WHEN** a POST request is received at `/applications/login` with a valid `applicationId`, `last4SSN`, and `dateOfBirth`
- **THEN** the system SHALL locate the application by `applicationId`
- **THEN** the system SHALL delegate credential verification to the `VerificationPort` (stubbed)
- **THEN** on successful verification, the system SHALL return a short-lived session token (JWT, 30-minute expiry) and the current `applicationStatus`

#### Scenario: Application not found
- **WHEN** a POST request is received with an `applicationId` that does not exist
- **THEN** the system SHALL return HTTP 404 with error code `APPLICATION_NOT_FOUND`

#### Scenario: Verification fails — incorrect credentials
- **WHEN** the `VerificationPort` returns a verification failure for the provided `last4SSN` and `dateOfBirth`
- **THEN** the system SHALL return HTTP 401 with error code `APPLICANT_VERIFICATION_FAILED`
- **THEN** the system SHALL NOT return any application details

#### Scenario: Terminal application cannot be re-entered
- **WHEN** a POST request is received for an application in a terminal state (DECLINED, FUNDED, COMPLETED, CANCELLED, EXPIRED)
- **THEN** the system SHALL return HTTP 422 with error code `APPLICATION_NOT_ACCESSIBLE`

---

### Requirement: Verification Port Stub
The system SHALL implement the `VerificationPort` as a stub that validates credentials against application data held in the application-management-service database. The stub SHALL be replaceable by a dedicated verification-service adapter without domain changes.

#### Scenario: Stub validates last4SSN and DOB
- **WHEN** the stub `VerificationPort` receives `applicationId`, `last4SSN`, and `dateOfBirth`
- **THEN** the stub SHALL retrieve the applicant record for the application
- **THEN** the stub SHALL compare the last 4 characters of the stored SSN token against the provided `last4SSN`
- **THEN** the stub SHALL compare the stored `dateOfBirth` against the provided value
- **THEN** the stub SHALL return success only if both match

#### Scenario: Stub is disabled in production via feature flag
- **WHEN** the `verification.stub.enabled` configuration property is `false`
- **THEN** the system SHALL reject all login requests with HTTP 503 and error code `VERIFICATION_SERVICE_UNAVAILABLE`
- **THEN** the real `VerificationPort` adapter SHALL be wired in when the verification-service is available

---

### Requirement: Application Status in Login Response
The system SHALL include the current application status in the login response so the UI shell can route the applicant to the correct micro-frontend immediately on re-entry.

#### Scenario: Status returned with session token
- **WHEN** login is successful
- **THEN** the response SHALL include: `sessionToken`, `expiresAt`, `applicationId`, `applicationStatus`
- **THEN** the UI shell SHALL use `applicationStatus` to route to the appropriate micro-frontend without an additional API call
