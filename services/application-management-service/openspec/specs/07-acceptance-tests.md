# 07 - Acceptance Tests

# Personal Loan Application Management

Version: 1.0

Status: Draft

---

# Feature

Application Intake and Application Creation

---

# Scenario 1 - Create Application Using Valid Invitation

## Given

A prospect has received a valid Personal Loan invitation.

And

The invitation exists in the Offer Management Platform.

And

The offer contains a valid customer reference identifier.

---

## When

The prospect enters the invitation identifier.

---

## Then

The system shall:

* Validate the invitation.
* Retrieve the associated offer.
* Retrieve customer information.
* Create an application intake context.
* Create a new application.
* Associate the application with the intake context.

---

# Scenario 2 - Invitation Expired

## Given

A prospect has an expired invitation.

---

## When

The prospect attempts to start an application.

---

## Then

The system shall:

* Reject the invitation.
* Not create an application.
* Return invitation expired error.

Example:

```json
{
 "errorCode":"INVITATION_EXPIRED",
 "message":"Invitation has expired"
}
```

---

# Scenario 3 - Invitation Not Found

## Given

The invitation identifier does not exist.

---

## When

The prospect starts application using invitation.

---

## Then

The system shall:

* Reject the request.
* Not create intake context.
* Not create application.

---

# Scenario 4 - Successful Customer Prefill

## Given

A valid invitation exists.

And

The offer contains customer reference information.

And

Customer information exists.

---

## When

Application Intake completes processing.

---

## Then

The system shall provide:

* Customer name.
* Address.
* Offer details.

for application prefill.

Phone and email are not prefilled. The applicant enters their own
contact details when completing the application form.

---

# Scenario 5 - Customer Information Unavailable

## Given

A valid invitation exists.

And

Offer retrieval succeeds.

But

Customer profile lookup fails.

---

## When

The prospect starts application.

---

## Then

The system shall:

* Create application intake context.
* Create application.
* Mark prefill status as incomplete.
* Allow applicant to manually enter missing information.

---

# Scenario 6 - Create Direct Application Without Invitation

## Given

A prospect does not have an invitation.

---

## When

The prospect starts a new application.

---

## Then

The system shall:

* Create direct application intake context.
* Set application source as DIRECT.
* Create application.
* Allow applicant to enter required information.

---

# Scenario 7 - Application Resume

## Given

An applicant has an existing incomplete application.

---

## When

The applicant resumes the application.

---

## Then

The system shall:

* Retrieve application.
* Retrieve saved information.
* Restore application state.
* Allow applicant to continue.

---

# Scenario 8 - Application Cannot Submit Incomplete Data

## Given

An application is missing required information.

---

## When

Applicant attempts submission.

---

## Then

The system shall:

* Validate application completeness.
* Reject submission.
* Return missing information details.

---

# Scenario 9 - Successful Application Submission

## Given

An application contains all required information.

---

## When

Applicant submits application.

---

## Then

The system shall:

* Change application status to SUBMITTED.
* Publish ApplicationSubmitted event.
* Send application for downstream processing.

---

# Scenario 10 - Offer Platform Failure

## Given

Offer Management Platform is unavailable.

---

## When

Invitation intake is initiated.

---

## Then

The system shall:

* Handle external failure.
* Not create incomplete application.
* Return temporary failure response.

---

# Scenario 11 - Duplicate Application Prevention

## Given

An applicant already has an active application created from an invitation.

An active application is one in status CREATED, IN_PROGRESS,
READY_FOR_SUBMISSION, SUBMITTED, or PROCESSING.

---

## When

The applicant attempts to start another application using the same invitation.

---

## Then

The system shall:

* Detect the existing active application.
* Reject the intake request.
* Not create a new IntakeContext or Application.
* Return error DUPLICATE_APPLICATION.

---

# Scenario 11b - Invitation Reuse After Terminal Application

## Given

A prospect previously created an application from an invitation.

That application has reached a terminal status: CANCELLED or EXPIRED.

---

## When

The prospect attempts to use the same invitation again.

---

## Then

The system shall:

* Detect no active application exists for the invitation.
* Allow intake to proceed.
* Create a new InvitationSession and IntakeContext.

---

# Scenario 13 - Expired Invitation Session

## Given

A prospect successfully initialized an invitation.

The InvitationSession has expired (30 minutes elapsed without application creation).

---

## When

The prospect attempts to create an application using the expired session.

---

## Then

The system shall:

* Detect the session has expired.
* Reject the application creation request.
* Return error INTAKE_EXPIRED.
* Require the prospect to re-initialize the invitation to obtain a new session.

---

# Scenario 12 - Audit Event Generation

## Given

An application journey is initiated.

---

## When

Application Intake or Application Creation occurs.

---

## Then

The system shall capture:

* Intake source.
* Timestamp.
* Application identifier.
* Business event.

---

# Non Functional Acceptance Criteria

## Performance

Application initialization should complete within:

Less than 2 seconds

excluding external dependency latency.

---

## Security

The system shall:

* Protect customer information.
* Avoid logging sensitive data.
* Apply authentication and authorization.

---

## Reliability

External dependency failures shall:

* Be handled gracefully.
* Support retry where applicable.
* Produce traceable errors.

---

# Related Documents

001-acquisition-business-capabilities.md

002-application-intake-spec.md

003-application-spec.md

04-sequence-diagrams.md

05-api-contracts.md

06-persistence-model.md
