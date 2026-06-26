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

For ITA applications, firstName, lastName, and address are prefilled from
Customer Profile (read-only on the form). Phone, email, SSN, citizenship,
and employment details are always entered by the applicant.

SSN is encrypted at rest and masked in all API responses (last 4 digits only).
SSN is never logged.

Attributes:

* applicantId
* firstName
* lastName
* dateOfBirth
* citizenship
* ssn (encrypted)
* address
* phone
* email
* employerName
* employmentStatus
* annualIncome

Citizenship Values:

* US_CITIZEN
* PERMANENT_RESIDENT
* DACA
* OTHER

Employment Status Values:

* EMPLOYED
* SELF_EMPLOYED
* RETIRED
* OTHER

---

# Entity: LoanRequest

Requested loan details provided by the applicant.

Attributes:

* loanRequestId
* requestedAmount
* termMonths
* loanPurpose

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
