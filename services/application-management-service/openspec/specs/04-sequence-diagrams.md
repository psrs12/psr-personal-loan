# 04 - Sequence Diagrams

# Personal Loan Application Management

Version: 1.0

Status: Draft

---

# 1. Overview

This document describes the application journey flows for Application Management.

Application Management supports multiple application initiation paths:

1. Invitation Intake
2. Direct Intake

Both paths converge into Application Creation and follow the same application lifecycle.

---

# 2. Invitation Based Intake and Application Creation

## Purpose

Shows the ITA flow. Invitation processing produces prefill data only.
The prospect completes remaining fields before Application is created.

## Step 1 - Initialize Invitation Intake

```mermaid
sequenceDiagram

actor Prospect

participant UI as Application UI
participant Intake as Application Intake
participant Offer as Offer Management Platform
participant Customer as Customer Profile Platform
participant DB as Application Database


Prospect ->> UI: Enter Invitation ID

UI ->> Intake: POST /invitations/initialize


Intake ->> Offer: Validate Invitation


Offer -->> Intake: Valid Invitation + Offer Details


Intake ->> Offer: Retrieve Offer


Offer -->> Intake: Offer Information


Intake ->> Customer: Retrieve Customer Profile


Customer -->> Intake: Customer Information


Intake ->> DB: Create InvitationSession + IntakeContext


DB -->> Intake: Intake Created


Intake -->> UI: { intakeId, offer, customer }


UI -->> Prospect: Display Prefilled Application Form
```

## Step 2 - Create Application

Prospect reviews prefilled data, completes remaining required fields, then submits.

```mermaid
sequenceDiagram

actor Prospect

participant UI as Application UI
participant App as Application Management
participant DB as Application Database


Prospect ->> UI: Complete and Submit Form

UI ->> App: POST /applications { intakeId, applicant, loanRequest }


App ->> DB: Persist Application


DB -->> App: Application Created


App -->> UI: { applicationId, status: CREATED }


UI -->> Prospect: Application Started
```

---

# 3. Direct Application Creation

## Purpose

Shows the flow when a prospect starts without an invitation.
No prefill data is available. Prospect enters all information manually.

## Step 1 - Initialize Direct Intake

```mermaid
sequenceDiagram

actor Prospect

participant UI as Application UI
participant Intake as Application Intake
participant DB as Application Database


Prospect ->> UI: Start Application

UI ->> Intake: POST /intake/direct


Intake ->> DB: Create IntakeContext (source = DIRECT)


DB -->> Intake: Intake Created


Intake -->> UI: { intakeId }


UI -->> Prospect: Display Empty Application Form
```

## Step 2 - Create Application

Prospect completes all required fields then submits.

```mermaid
sequenceDiagram

actor Prospect

participant UI as Application UI
participant App as Application Management
participant DB as Application Database


Prospect ->> UI: Complete and Submit Form

UI ->> App: POST /applications { intakeId, applicant, loanRequest }


App ->> DB: Persist Application


DB -->> App: Application Created


App -->> UI: { applicationId, status: CREATED }


UI -->> Prospect: Application Started
```

---

# 4. Application Resume Flow

## Purpose

Allows prospect to continue an existing application.

```mermaid
sequenceDiagram

actor Prospect

participant UI as Application UI
participant App as Application Management
participant DB as Application Database


Prospect ->> UI: Resume Application

UI ->> App: Retrieve Application


App ->> DB: Load Application


DB -->> App: Application Data


App -->> UI: Application State


UI -->> Prospect: Continue Journey
```

---

# 5. Invalid Invitation Flow

## Purpose

Handles invalid or expired invitation.

```mermaid
sequenceDiagram

actor Prospect

participant UI
participant Intake as Application Intake
participant Offer as Offer Management Platform


Prospect ->> UI: Enter Invitation ID


UI ->> Intake: Initialize Intake


Intake ->> Offer: Validate Invitation


Offer -->> Intake: Invalid Invitation


Intake -->> UI: Invitation Error


UI -->> Prospect: Display Error
```

---

# 6. Offer Retrieval Failure

```mermaid
sequenceDiagram

participant Intake as Application Intake
participant Offer as Offer Management Platform


Intake ->> Offer: Retrieve Offer


Offer --x Intake: System Failure


Intake ->> Intake: Handle Failure


Intake -->> UI: Temporary Error
```

---

# 7. Customer Profile Failure

Customer lookup failure does not block the intake. IntakeContext is created
with prefillStatus = PARTIAL. Offer data is still returned. Prospect must
manually enter customer information before creating the application.

```mermaid
sequenceDiagram

participant Intake as Application Intake
participant Customer as Customer Profile Platform
participant DB as Application Database
participant UI


Intake ->> Customer: Retrieve Customer


Customer --x Intake: Customer Lookup Failure


Intake ->> DB: Create IntakeContext (prefillStatus = PARTIAL)


DB -->> Intake: Intake Created


Intake -->> UI: { intakeId, offer, prefillStatus: PARTIAL }


UI -->> UI: Display form with offer prefilled, customer fields empty
```

---

# 8. Application Submission Flow

```mermaid
sequenceDiagram

actor Applicant

participant UI
participant App as Application Management
participant Decision as Decision Platform


Applicant ->> UI: Submit Application


UI ->> App: Submit Application


App ->> App: Validate Completeness


App ->> Decision: Send Application


Decision -->> App: Decision Response


App -->> UI: Application Status
```

---

# 9. Key Design Notes

## Application Management owns

* Application lifecycle
* Application state
* Applicant information
* Submission

## Application Intake owns

* Entry channel determination
* Invitation processing
* Direct entry initialization
* Prefill preparation

## External Systems own

Offer Management:

* Invitation validation
* Offer lifecycle

Customer Platform:

* Customer profile

Decision Platform:

* Credit decisioning

```
```
