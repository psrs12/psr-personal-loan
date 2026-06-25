# Domain Model

## Bounded Context

Application Management

## Subdomains

### Invitation Processing

Validates invitations and retrieves prefill information.

### Application Creation

Creates applications from invitation or direct channels.

### Application Maintenance

Maintains application state.

### Application Submission

Submits completed applications.

---

# Aggregate: InvitationSession

Represents an invitation validation session.

Attributes:

* sessionId
* applicationSource
* invitationId
* offerId
* customerReferenceId
* status
* createdTimestamp


Statuses:


* VALIDATED
* COMPLETED
* FAILED
* EXPIRED

---

# Aggregate: Application

Represents a personal loan application.

Attributes:

* applicationId
* applicantId
* applicationStatus
* channel
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
