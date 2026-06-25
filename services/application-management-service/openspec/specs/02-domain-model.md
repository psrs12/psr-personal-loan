# Domain Model

## Bounded Context

Application Management

## Subdomains

### Invitation Processing

Validates invitations and retrieves prefill information.

### Application Intake

Creates intake context from invitation or direct channels.

### Application Creation

Creates applications from intake context.

### Application Maintenance

Maintains application state.

### Application Submission

Submits completed applications.

---

# Aggregate Relationships

```
InvitationSession ──produces──▶ ApplicationIntakeContext ──initialises──▶ Application
(ITA flow only)                  (all channels)

Direct path:
                 ApplicationIntakeContext ──initialises──▶ Application
                 (source = DIRECT, no session)
```

---

# Aggregate: InvitationSession

Represents the process of validating an invitation with external systems.

Owned by the Invitation Processing subdomain.

Created when a prospect submits an invitation identifier. Tracks the multi-step interaction with Offer Management and Customer Profile platforms. Produces prefill data consumed by ApplicationIntakeContext.

Attributes:

* sessionId
* invitationId
* offerId
* customerReferenceId
* status
* createdTimestamp
* expirationTimestamp

Statuses:

* RECEIVED
* VALIDATED
* OFFER_RETRIEVED
* CUSTOMER_RETRIEVED
* COMPLETED
* FAILED
* EXPIRED

---

# Aggregate: ApplicationIntakeContext

Represents the origin and initialisation state of an application journey.

Owned by the Application Intake subdomain.

Created after InvitationSession completes (ITA flow) or directly (DIRECT flow). Channel-agnostic. Carries the intake source, offer reference, customer reference, and prefill status into Application creation.

Attributes:

* intakeId
* applicationSource
* invitationId (populated for INVITATION source only)
* offerId (populated for INVITATION source only)
* customerReferenceId (populated for INVITATION source only)
* prefillStatus
* createdTimestamp

Application Source Values:

* INVITATION
* DIRECT
* PARTNER

Prefill Status Values:

* NOT_APPLICABLE
* COMPLETE
* PARTIAL
* FAILED

---

# Aggregate: Application

Represents a personal loan application.

Owned by the Application Creation and Application Maintenance subdomains.

Initialised from an ApplicationIntakeContext. Maintains application lifecycle from creation through submission. For INVITATION source, persists offer terms at creation time as an ApplicationOffer entity.

Attributes:

* applicationId
* intakeId
* applicationSource
* applicationStatus
* applicant (entity)
* loanRequest (entity)
* applicationOffer (entity, present for INVITATION source only)
* createdTimestamp
* updatedTimestamp

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

# Entity: ApplicationOffer

Represents offer terms snapshotted at application creation time.

Owned by the Application aggregate. Persisted independently of the intake
offer snapshot to support long-term audit retention.

Present for INVITATION source applications only.

customerReferenceId is included as it is part of the offer data returned by
Offer Management. Customer details (name, address, contact) retrieved from
Customer Profile for prefill are NOT part of this entity — those are PII
used only to populate the application form and are captured in the Applicant
entity as confirmed by the applicant.

Attributes:

* applicationOfferId
* offerId
* customerReferenceId
* loanAmount
* apr
* termMonths
* expirationDate
* capturedTimestamp
