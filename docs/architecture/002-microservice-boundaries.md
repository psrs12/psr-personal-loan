# architecture/002-microservice-boundaries.md

# Personal Loan Acquisition Platform

## Microservice Boundary Specification

Version: 1.0

Status: Draft

Owner: Enterprise Architecture

---

# 1. Purpose

This document defines the microservice decomposition strategy for the Personal Loan Acquisition Platform.

The objective is to establish:

* Clear service ownership
* Domain-driven boundaries
* Data ownership
* API ownership
* Deployment boundaries
* Integration responsibilities

---

# 2. Microservice Principles

## Principle 1

Each microservice owns one business capability.

---

## Principle 2

Each microservice owns its data.

No shared database.

---

## Principle 3

Services communicate through:

* APIs
* Events

---

## Principle 4

Enterprise capabilities are integrated, not duplicated.

---

## Principle 5

Services must be independently deployable.

---

# 3. Service Landscape

```text
Personal Loan Acquisition Platform

+------------------------------------------------+
|                Acquisition Domain              |
+------------------------------------------------+

application-service

invitation-service

offer-acceptance-service

document-service

underwriting-service

application-tracking-service


+------------------------------------------------+
|             Orchestration Domain               |
+------------------------------------------------+

identity-orchestration-service

fraud-orchestration-service

credit-orchestration-service

decision-orchestration-service

funding-orchestration-service

notification-orchestration-service
```

---

# 4. Core Acquisition Services

---

# 4.1 Application Service

## Purpose

Central owner of application lifecycle.

---

## Responsibilities

Own application creation.

Manage application status.

Manage applicant information.

Manage application workflow state.

Publish application events.

---

## Owns Data

Application

Applicant

Application Status

Application History

---

## APIs Owned

POST /applications

GET /applications/{id}

PUT /applications/{id}

POST /applications/{id}/submit

---

## Events Published

ApplicationCreated

ApplicationUpdated

ApplicationSubmitted

ApplicationStatusChanged

---

## Does Not Own

Credit

Fraud

Decision

Offer

Funding

---

# 4.2 Invitation Service

## Purpose

Handle Invitation To Apply journey.

---

## Responsibilities

Validate invitation.

Retrieve offer reference.

Initialize application.

Track invitation usage.

---

## Integrates With

Offer Management Platform

---

## Owns Data

Invitation Reference

Invitation Usage Audit

---

## Does Not Own

Campaign

Offer

Prospect

Pricing

---

## APIs Owned

POST /invitations/validate

---

# 4.3 Offer Acceptance Service

## Purpose

Capture customer acceptance of approved offers.

---

## Responsibilities

Display offer.

Capture acceptance.

Capture disclosures.

Capture consent.

---

## Owns Data

Acceptance Record

Consent Record

Acceptance Timestamp

---

## Events Published

OfferAccepted

ConsentCaptured

---

# 4.4 Document Service

## Purpose

Manage document collection workflow.

---

## Responsibilities

Create document requests.

Track document status.

Manage missing documents.

---

## Owns Data

Document Requirement

Document Status

Document Metadata

---

## Does Not Own

Binary storage

Retention policy

---

# 4.5 Underwriting Service

## Purpose

Manage manual review workflow.

---

## Responsibilities

Create review tasks.

Assign reviewers.

Track conditions.

Capture review outcome.

---

## Owns Data

Review Tasks

Conditions

Underwriting Status

---

# 4.6 Application Tracking Service

## Purpose

Provide application visibility.

---

## Responsibilities

Customer status.

Application timeline.

Progress tracking.

---

## Owns Data

Tracking View

Timeline Events

---

# 5. Enterprise Integration Services

---

# 5.1 Identity Orchestration Service

## Purpose

Adapter between acquisition and enterprise identity platform.

---

## Responsibilities

Create verification request.

Handle response.

Update workflow.

---

## Integrates With

Enterprise Identity Platform

---

## Owns

Verification Reference

Verification Status

---

## Does Not Own

Identity data

Identity score

---

# 5.2 Fraud Orchestration Service

## Purpose

Coordinate fraud verification.

---

## Responsibilities

Submit fraud request.

Process fraud response.

Apply personal loan fraud workflow rules.

---

## Integrates With

Enterprise Fraud Platform

---

## Owns

Fraud Workflow State

Fraud Request Tracking

---

## Does Not Own

Fraud Decision

Fraud Model

Fraud Score

Fraud Audit

---

# 5.3 Credit Orchestration Service

## Purpose

Coordinate credit evaluation.

---

## Integrates With

Enterprise Credit Management Platform

---

## Owns

Credit Request

Credit Status

Credit Response Reference

---

## Does Not Own

Credit Model

Credit Score

Credit Data

---

# 5.4 Decision Orchestration Service

## Purpose

Coordinate decision execution.

---

## Integrates With

Enterprise Decision Platform

---

## Owns

Decision Request

Decision Status

Decision Response Reference

---

## Does Not Own

Decision Rules

Risk Models

Decision Logic

---

# 5.5 Funding Orchestration Service

## Purpose

Coordinate funding request.

---

## Integrates With

Enterprise Funding Platform

---

## Owns

Funding Request

Funding Status

Funding Tracking

---

## Does Not Own

Disbursement

Loan Account

Payments

---

# 5.6 Notification Orchestration Service

## Purpose

Coordinate customer communication.

---

## Integrates With

Notification Platform

---

## Owns

Notification Request

Communication History

---

# 6. Shared Platform Services

---

# API Gateway

Responsibilities:

Authentication

Routing

Rate Limiting

Security

---

# Audit Service

Responsibilities:

Business audit

Compliance history

Event capture

---

# Configuration Service

Responsibilities:

Runtime configuration

Feature flags

---

# 7. Database Ownership Model

Each service:

Own database

Own schema

Own migrations

Example:

```text
application-db

invitation-db

offer-acceptance-db

document-db

underwriting-db

tracking-db

identity-orchestration-db

fraud-orchestration-db

credit-orchestration-db

decision-orchestration-db

funding-db
```

---

# 8. Communication Matrix

| Service                     | Communication |
| --------------------------- | ------------- |
| Application → Fraud         | Event/API     |
| Application → Credit        | Event/API     |
| Application → Decision      | API           |
| Application → Offer         | API           |
| Application → Funding       | Event/API     |
| All Services → Notification | Event         |

---

# 9. Deployment Model

Each service:

* Own container
* Own deployment pipeline
* Own scaling policy
* Own monitoring

---

# 10. Anti-Patterns Avoided

## Shared Database

Not allowed.

---

## Distributed Business Logic

Not allowed.

---

## Enterprise Capability Duplication

Not allowed.

---

## Tight Service Coupling

Avoided using events.

---

# 11. Related Documents

000-architecture-overview.md

001-logical-architecture.md

003-data-architecture.md

004-api-standards.md

005-event-driven-architecture.md

006-integration-patterns.md

007-security-architecture.md
