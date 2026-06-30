# 07 - Acceptance Tests

# Personal Loan Application Management

Version: 1.0

Status: Active

---

# Feature: SSN Verification

## Scenario 1 — SSN verified successfully

**Given** a valid 9-digit SSN is submitted to `POST /ssn/verify`

**And** the SSN Verification Service returns a verification token

**When** the request is processed

**Then** the response shall be `200 OK` with `verificationToken` and `expiresAt`

**And** the Bolt Tokenisation Platform shall be called to pre-warm or validate the SSN

**And** the raw SSN shall not be stored or logged

---

## Scenario 2 — SSN verification fails

**Given** a 9-digit SSN that cannot be verified is submitted to `POST /ssn/verify`

**When** the SSN Verification Service returns a failure

**Then** the response shall be `422` with error code `SSN_VERIFICATION_FAILED`

**And** no verification token shall be issued

---

## Scenario 3 — SSN format invalid

**Given** an SSN submitted with dashes (e.g. `"123-45-6789"`) or fewer than 9 digits

**When** `POST /ssn/verify` is called

**Then** the response shall be `400 Bad Request` due to `@Pattern(regexp = "\\d{9}")` validation failure

---

# Feature: Application Creation — Direct Path

## Scenario 4 — Direct application created successfully

**Given** a valid `ssnVerificationToken` exists in the token store (not expired)

**And** all required fields are present: `ssn` (9 digits), `firstName`, `lastName`, `dateOfBirth`, `citizenship`, `email`, `phone`, `street`, `city`, `state` (2 chars), `zip`, `annualIncome`, `requestedAmount` (>= 1000), `termMonths` (6–84)

**When** `POST /applications` is called without an `intakeId`

**Then** the response shall be `200 OK` with `applicationId` and `applicationSource: DIRECT`

**And** the Bolt Tokenisation Platform shall be called with the raw SSN

**And** the `applicant` record shall store `ssn_token` (Bolt reference) — not the raw SSN

**And** no raw SSN shall appear in the database or any log output

---

## Scenario 5 — SSN verification token missing or expired

**Given** an `ssnVerificationToken` that does not exist in the token store or has expired

**When** `POST /applications` is called

**Then** the response shall be `400 Bad Request` with error code `SSN_VERIFICATION_TOKEN_INVALID`

**And** no application shall be created

---

## Scenario 6 — Missing Authorization header

**Given** a valid request body

**When** `POST /applications` is called without `Authorization: Bearer <token>`

**Then** the response shall be `401 Unauthorized`

---

## Scenario 7 — Citizenship field validation

**Given** an application request with an invalid `citizenship` value (not in enum)

**When** `POST /applications` is called

**Then** the response shall be `400 Bad Request`

**And** no application shall be created

---

## Scenario 8 — Annual income below minimum

**Given** an application request with `annualIncome: -1`

**When** `POST /applications` is called

**Then** the response shall be `400 Bad Request` due to `@DecimalMin("0.00")` validation

---

## Scenario 9 — Term months out of range

**Given** an application request with `termMonths: 120` (exceeds maximum of 84)

**When** `POST /applications` is called

**Then** the response shall be `400 Bad Request` due to `@Max(84)` validation

---

## Scenario 10 — Requested amount below minimum

**Given** an application request with `requestedAmount: 500.00` (below minimum of $1,000)

**When** `POST /applications` is called

**Then** the response shall be `400 Bad Request` due to `@DecimalMin("1000.00")` validation

---

# Feature: Application Creation — ITA Path

## Scenario 11 — ITA application created successfully

**Given** a valid invitation has been initialised and an `intakeId` was returned

**And** a valid `ssnVerificationToken` exists

**And** all required fields are provided (note: `firstName`, `lastName`, `street`, `city`, `state`, `zip` are prefilled from Customer Profile but still submitted in the request)

**When** `POST /applications` is called with the `intakeId`

**Then** the response shall be `200 OK` with `applicationSource: INVITATION`

**And** the application shall be associated with the `ApplicationIntakeContext`

---

## Scenario 12 — Invitation expired

**Given** an invitation identifier that has passed its expiry date

**When** `POST /invitations/initialize` is called

**Then** the response shall be `409` with error code `INVITATION_EXPIRED`

**And** no `InvitationSession` or `ApplicationIntakeContext` shall be created

---

## Scenario 13 — Invitation not found

**Given** an invitation identifier that does not exist in the Offer Management Platform

**When** `POST /invitations/initialize` is called

**Then** the response shall be `404` with error code `INVITATION_NOT_FOUND`

---

## Scenario 14 — Customer profile unavailable — partial prefill

**Given** a valid invitation where Offer Management Platform succeeds

**But** the Customer Profile Platform is unavailable

**When** `POST /invitations/initialize` is called

**Then** the response shall be `200 OK` with `prefillStatus: PARTIAL`

**And** offer details shall be present in the response

**And** the `customer` block shall be absent or empty

**And** the applicant shall manually enter name and address fields

---

## Scenario 15 — Duplicate application prevention

**Given** an applicant already has an active application (status: `CREATED`, `IN_PROGRESS`, `SUBMITTED`, or `PROCESSING`) from the same invitation

**When** the applicant attempts to create another application with the same invitation

**Then** the response shall be `409` with error code `DUPLICATE_APPLICATION`

**And** no new `InvitationSession`, `IntakeContext`, or `Application` shall be created

---

## Scenario 16 — Invitation session expired before application creation

**Given** a prospect initialised an invitation but did not create an application within 30 minutes

**When** `POST /applications` is called with the expired `intakeId`

**Then** the response shall be `409` with error code `INTAKE_EXPIRED`

**And** the prospect must re-initialise the invitation to obtain a new session

---

# Feature: Application Lifecycle

## Scenario 17 — Application submission

**Given** an application with all required information in status `IN_PROGRESS`

**When** `POST /applications/{id}/submit` is called

**Then** the application status shall transition to `SUBMITTED`

**And** an `ApplicationSubmitted` event shall be published

---

## Scenario 18 — Submission rejected — incomplete application

**Given** an application missing required fields

**When** `POST /applications/{id}/submit` is called

**Then** the response shall be `422` with validation details

**And** the application status shall remain unchanged

---

# Feature: Applicant Login

## Scenario 19 — Successful login

**Given** an application exists with a stored `ssnToken`

**And** the applicant provides `applicationId`, `last4SSN` matching the last 4 characters of `ssnToken`, and correct `dateOfBirth`

**When** `POST /applications/login` is called (no Authorization header required)

**Then** the response shall be `200 OK` with `sessionToken` (JWT, 30-minute expiry), `expiresAt`, `applicationId`, and `applicationStatus`

---

## Scenario 20 — Login rejected — wrong credentials

**Given** an application exists

**When** `POST /applications/login` is called with an incorrect `last4SSN` or `dateOfBirth`

**Then** the response shall be `401` with error code `APPLICANT_VERIFICATION_FAILED`

**And** no application details shall be returned

---

## Scenario 21 — Login rejected — terminal application

**Given** an application in a terminal state (`DECLINED`, `FUNDED`, `COMPLETED`, `CANCELLED`, `EXPIRED`)

**When** `POST /applications/login` is called

**Then** the response shall be `422` with error code `APPLICATION_NOT_ACCESSIBLE`

---

# Feature: Application Timeline

## Scenario 22 — Timeline returned in chronological order

**Given** a DIRECT application has been created and an `APPLICATION_CREATED` audit record exists

**When** `GET /applications/{id}/timeline` is called with a valid `Authorization` header

**Then** the response shall be `200 OK` with `applicationId` and a non-empty `events` array

**And** the first event shall have `eventType: APPLICATION_CREATED`

**And** events shall be ordered ascending by `eventTimestamp`

**And** each event shall contain `auditId`, `eventType`, `eventTimestamp`, `intakeId` (nullable), and `payload`

---

## Scenario 23 — Timeline for unknown application returns 404

**Given** a random UUID that does not correspond to any application

**When** `GET /applications/{id}/timeline` is called with a valid `Authorization` header

**Then** the response shall be `404 Not Found`

---

## Scenario 24 — Timeline requires Authorization header

**Given** a valid `applicationId`

**When** `GET /applications/{id}/timeline` is called **without** an `Authorization` header

**Then** the response shall be `401 Unauthorized`

---

# Non-Functional Acceptance Criteria

## Performance

- `POST /ssn/verify` shall respond within 1 second excluding SSN Verification Service latency
- `POST /applications` shall respond within 2 seconds excluding Bolt Tokenisation latency
- `POST /applications/login` shall respond within 500ms

## Security

- Raw SSN must never appear in any log line — enforced by `SensitiveDataMaskingConverter`
- `ssn_token` must never be returned in any API response
- All endpoints except `POST /applications/login` require `Authorization: Bearer <token>`
- Session tokens expire after 30 minutes

## Idempotency

- Duplicate `ApplicationCreated` events must not create duplicate applications

---

# Related Documents

```
services/application-management-service/openspec/specs/001-capability-spec.md
services/application-management-service/openspec/specs/02-domain-model.md
services/application-management-service/openspec/specs/04-sequence-diagrams.md
services/application-management-service/openspec/specs/05-api-contracts.md
services/application-management-service/openspec/specs/06-persistence-model.md
openspec/application-management/spec.md
```
