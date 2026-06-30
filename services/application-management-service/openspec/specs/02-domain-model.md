# Domain Model

## Bounded Context

Application Management

## Subdomains

### Invitation Processing

Validates invitations and retrieves prefill information (name, address only).

### Application Creation

Creates applications from invitation or direct channels. SSN verified before creation.

### Application Maintenance

Maintains application state.

### Application Submission

Submits completed applications.

---

# Aggregate Relationships

```
ITA path:
InvitationSession ──produces──▶ ApplicationIntakeContext ──initialises──▶ Application
(prefill: name, address)

DIRECT path:
                                                           Application
                                                           (no intake context, no prefill)
```

---

# Aggregate: InvitationSession

Represents an invitation validation session.

Tracks the multi-step interaction with Offer Management and Customer Profile platforms.
Expires 30 minutes after creation if no application has been created.

Attributes:

* sessionId
* applicationSource
* invitationId
* offerId
* customerReferenceId
* status
* createdTimestamp
* expirationTimestamp

Statuses:

* VALIDATED
* COMPLETED
* FAILED
* EXPIRED

---

# Aggregate: ApplicationIntakeContext

Represents the origin and initialisation state of an ITA application journey.

Present for INVITATION source only. DIRECT applications have no intake context.

Attributes:

* intakeId
* sessionId
* applicationSource
* invitationId
* offerId
* customerReferenceId
* prefillStatus
* createdTimestamp

Prefill Status Values:

* COMPLETE
* PARTIAL
* FAILED

---

# Aggregate: Application

Represents a personal loan application.

Created after SSN verification passes. For ITA path, initialised from an
ApplicationIntakeContext. For DIRECT path, created directly with no intakeId.

Attributes:

* applicationId
* intakeId (nullable — present for INVITATION source only)
* applicationSource
* applicationStatus
* applicant (entity)
* loanRequest (entity)
* applicationOffer (entity, present for INVITATION source only)
* createdTimestamp
* updatedTimestamp

Application Source Values:

* INVITATION
* DIRECT
* PARTNER

Statuses:

* CREATED
* IN_PROGRESS
* READY_FOR_SUBMISSION
* SUBMITTED
* APPROVED
* DECLINED
* CANCELLED
* EXPIRED

---

# Entity: Applicant

Applicant personal information as entered and confirmed by the prospect.

## ITA vs Direct — Prefill Rules

For ITA applications, `firstName`, `lastName`, `street`, `city`, `state`, and `zip` are prefilled
from the Customer Profile Platform and are **read-only on the application form**.

The following fields are **always entered by the applicant** regardless of channel:

* `phone`, `email`
* `dateOfBirth`, `citizenship`
* `ssn` (captured, verified, and tokenised — never stored raw)
* `employerName`, `employmentStatus`, `annualIncome`

---

## Attributes

| Field | Type | Nullable | Notes |
|-------|------|----------|-------|
| `applicantId` | UUID | No | PK, assigned at creation |
| `applicationId` | UUID | No | FK to application |
| `firstName` | String | No | Read-only for ITA; entered for DIRECT |
| `lastName` | String | No | Read-only for ITA; entered for DIRECT |
| `dateOfBirth` | LocalDate | No | Always entered by applicant |
| `citizenship` | Citizenship | No | Enum — see values below |
| `ssnToken` | String | No | Bolt tokenisation reference — never raw SSN |
| `email` | String | No | Always entered by applicant |
| `phone` | String | No | Always entered by applicant |
| `street` | String | No | Read-only for ITA; entered for DIRECT |
| `city` | String | No | Read-only for ITA; entered for DIRECT |
| `state` | String (2) | No | 2-character US state code |
| `zip` | String | No | Read-only for ITA; entered for DIRECT |
| `employerName` | String | Yes | Optional — not required for RETIRED |
| `employmentStatus` | EmploymentStatus | Yes | Enum — see values below |
| `annualIncome` | BigDecimal | No | Minimum 0.00 |
| `createdTimestamp` | LocalDateTime | No | Set at creation |

---

## Enum: Citizenship

```
US_CITIZEN
PERMANENT_RESIDENT
DACA
OTHER
```

---

## Enum: EmploymentStatus

```
EMPLOYED
SELF_EMPLOYED
RETIRED
OTHER
```

`employerName` is optional when `employmentStatus` is `RETIRED`.

---

## SSN Handling

SSN is never stored in plaintext. The capture and storage flow is:

```
1. Applicant enters SSN on the form (9 digits, no dashes)
2. UI calls POST /ssn/verify { ssn }
   → SSNVerificationPort: verifySSN(ssn)
   → Returns SSNVerificationToken { token, expiresAt (15 minutes) }
3. UI includes token + raw SSN in POST /applications
   → CreateApplicationUseCase:
       a. Validates SSNVerificationToken is not expired
       b. BoltTokenizationPort: tokenize(ssn) → ssnToken (opaque reference)
       c. ssnToken stored on Applicant entity
       d. Raw SSN is discarded — never persisted
```

### Value Object: SSNVerificationToken

```
token     String        — opaque verification token passed to POST /applications
expiresAt LocalDateTime — 15-minute TTL from issue time
isExpired()             — true if LocalDateTime.now() is after expiresAt
```

### Port: SSNVerificationPort

```
SSNVerificationToken verifySSN(String ssn)
```

Implemented by `SSNVerificationAdapter` (calls external SSN Verification Service).

### Port: BoltTokenizationPort

```
String tokenize(String ssn)
```

Implemented by `BoltTokenizationAdapter` (calls Bolt tokenisation platform). Returns an opaque `ssnToken` stored on the `Applicant`. Throws `TokenizationUnavailableException` if the platform is unreachable.

### SSN in Login Verification

The `StubVerificationAdapter` (used when `verification.stub.enabled=true`) verifies applicant login by comparing the last 4 characters of `ssnToken` against the `last4SSN` supplied in the login request. The stub is replaced by a `VerificationPort` adapter when a dedicated verification service is available.

### Rules

* SSN must never appear in any log line — enforced by `SensitiveDataMaskingConverter`
* SSN must never be returned in any API response — masked to last 4 digits only where displayed
* `ssnToken` is never exposed in API responses

---

# Entity: LoanRequest

Requested loan details provided by the applicant.

## Attributes

| Field | Type | Nullable | Constraints |
|-------|------|----------|-------------|
| `loanRequestId` | UUID | No | PK |
| `applicationId` | UUID | No | FK to application |
| `requestedAmount` | BigDecimal | No | Minimum $1,000.00 |
| `termMonths` | Integer | No | 6 – 84 months |
| `loanPurpose` | String | Yes | Free text or coded value |

---

# Entity: ApplicationOffer

Offer terms snapshotted at application creation time. Present for INVITATION
source only. Persisted for 7-year audit retention independent of the intake
offer_snapshot.

Stores customerReferenceId from the offer. Does NOT store customer PII
(name, address, phone, email) — those are in the Applicant entity.

Attributes:

* applicationOfferId
* offerId
* customerReferenceId
* loanAmount
* apr
* termMonths
* expirationDate
* capturedTimestamp
