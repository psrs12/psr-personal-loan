# 001-acquisition-business-capabilities.md

# Personal Loan Acquisition Platform

## Business Capability Model

Version: 3.0

Status: Draft

Owner: Enterprise Architecture

---

# 1. Purpose

This document defines the business capabilities owned by the Personal Loan Acquisition Platform.

The platform provides the end-to-end digital acquisition journey for personal loan applicants and orchestrates enterprise services required to evaluate, approve, and fund a loan application.

The platform supports multiple application entry channels including:

* Invitation To Apply (ITA)
* Direct Application
* Future acquisition channels

The platform does not own enterprise capabilities such as Offer Management, Identity Verification, Fraud Management, Credit Management, Decisioning, Notification Delivery, or Funding Execution.

---

# 2. Business Objectives

The platform shall:

* Provide a seamless digital loan application experience.
* Support invitation-based acquisition journeys.
* Support direct application journeys.
* Reuse available customer information when available.
* Reduce applicant data entry.
* Coordinate enterprise verification and decision services.
* Reduce application abandonment.
* Support straight-through processing.
* Support manual underwriting when required.
* Initiate loan funding requests.
* Maintain complete acquisition auditability.

---

# 3. Business Scope

## In Scope

Application Management

Application Intake

Application Creation

Customer Information Collection

Identity Verification Orchestration

Fraud Verification Orchestration

Credit Evaluation Orchestration

Decision Orchestration

Offer Acceptance

Document Collection

Underwriting Workflow

Bank Verification

Funding Request Orchestration

Notification Orchestration

Application Tracking

Acquisition Audit Trail

---

## Out Of Scope

Campaign Management

Offer Management

Prospect Management

Identity Management

Fraud Management

Credit Management

Decision Management

Funding Execution

Loan Booking

Loan Servicing

Payments

Statements

Collections

Recovery

Customer Relationship Management

---

# 4. Business Actors

## Customer Facing Actors

Prospect

Applicant

Customer

---

## Internal Actors

Underwriter

Fraud Analyst

Customer Service Representative

Funding Specialist

Operations Analyst

Compliance Analyst

---

## External Enterprise Systems

Offer Management Platform

Identity Verification Platform

Fraud Platform

Fraud / AML Platform

Credit Management Platform

Decision Platform

Document Platform

Notification Platform

Funding Platform

Core Loan Platform

Authentication Platform

Customer Profile Platform

---

# 5. Acquisition Journey

The acquisition journey supports multiple entry points.

## Invitation Based Journey

Invitation Intake

↓

Offer Retrieval

↓

Customer Information Retrieval

↓

Application Creation

↓

Application Completion

↓

Identity Verification

↓

Fraud Verification

↓

Credit Evaluation

↓

Decision Evaluation

↓

Offer Acceptance

↓

Document Collection

↓

Underwriting

↓

Bank Verification

↓

Funding Request

↓

Loan Booking

---

## Direct Application Journey

Direct Intake

↓

Application Creation

↓

Customer Information Collection

↓

Application Completion

↓

Identity Verification

↓

Fraud Verification

↓

Credit Evaluation

↓

Decision Evaluation

↓

Offer Acceptance

↓

Document Collection

↓

Underwriting

↓

Bank Verification

↓

Funding Request

↓

Loan Booking

---

# 6. Business Capabilities

# Capability 1 – Application Management

## Purpose

Manage the complete personal loan application lifecycle from application initiation through submission.

Application Management provides a unified application journey regardless of acquisition channel.

---

## Functions

Create Application

Save Application

Resume Application

Submit Application

Track Application Status

Manage Applicant Information

Maintain Application State

Capture Application Events

---

## Specification

003-application-spec.md

---

# Sub Capability – Application Intake

## Purpose

Initialize the application journey based on how the prospect enters the platform.

Application Intake determines available information and creates the initial application context.

---

## Intake Channel 1 – Invitation Intake

### Purpose

Support prospects who received a marketing invitation.

---

## Functions

Validate Invitation Identifier

Retrieve Offer Information

Retrieve Customer Reference Identifier

Retrieve Customer Profile Information

Create Application Intake Context

Prefill Available Application Information

Initialize Application Creation

---

## External Dependencies

Offer Management Platform

Customer Profile Platform

---

## Intake Channel 2 – Direct Intake

### Purpose

Support prospects who start a personal loan application without an invitation.

---

## Functions

Create Empty Application Context

Collect Applicant Information

Initialize Application Creation

Continue Application Journey

---

## Application Intake Context

Attributes:

intakeId

applicationSource

invitationId

offerId

customerReferenceId

prefillStatus

createdTimestamp

Application Source:

INVITATION

DIRECT

PARTNER

---

## Specification

002-invitation-to-apply-spec.md

---

# Capability 2 – Identity Verification Orchestration

## Purpose

Coordinate identity verification activities.

---

## Functions

Initiate Verification

Track Verification Status

Process Verification Results

Apply Product-Specific Verification Rules

---

## Specification

004-identity-verification-orchestration-spec.md

---

# Capability 3 – Fraud Verification Orchestration

## Purpose

Coordinate fraud evaluation activities.

---

## Functions

Initiate Fraud Evaluation

Track Fraud Status

Process Fraud Results

Route Fraud Exceptions

---

## Specification

005-fraud-verification-orchestration-spec.md

---

# Capability 4 – Credit Evaluation Orchestration

## Purpose

Coordinate credit evaluation activities.

---

## Functions

Initiate Credit Evaluation

Track Credit Status

Process Credit Results

Route Credit Exceptions

---

## Specification

006-credit-evaluation-orchestration-spec.md

---

# Capability 5 – Decision Orchestration

## Purpose

Coordinate decision processing.

---

## Functions

Submit Decision Requests

Track Decision Status

Process Decision Outcomes

Route Referred Applications

Manage Decision Exceptions

---

## Specification

007-decision-orchestration-spec.md

---

# Capability 6 – Offer Acceptance

## Purpose

Present approved offers and capture acceptance.

---

## Functions

Display Approved Offers

Capture Acceptance

Capture Disclosures

Capture Electronic Consent

---

## Specification

008-offer-acceptance-spec.md

---

# Capability 7 – Document Collection

## Purpose

Collect required applicant documents.

---

## Functions

Generate Document Requests

Upload Documents

Track Document Status

Manage Outstanding Requirements

---

## Specification

009-document-collection-spec.md

---

# Capability 8 – Underwriting Workflow

## Purpose

Support manual application review.

---

## Functions

Create Work Queues

Assign Reviews

Manage Conditions

Capture Review Decisions

Track Underwriting Status

---

## Specification

010-underwriting-spec.md

---

# Capability 9 – Bank Verification

## Purpose

Verify applicant funding accounts.

---

## Functions

Initiate Verification

Track Verification Results

Manage Exceptions

---

## Specification

011-bank-verification-spec.md

---

# Capability 10 – Funding Request Orchestration

## Purpose

Initiate loan funding.

---

## Functions

Create Funding Requests

Track Funding Status

Process Funding Responses

Manage Funding Exceptions

---

## Specification

012-funding-request-spec.md

---

# Capability 11 – Notification Orchestration

## Purpose

Coordinate applicant communications.

---

## Functions

Generate Notifications

Track Notification Status

Maintain Communication History

---

## Specification

013-notification-orchestration-spec.md

---

# Capability 12 – Application Tracking

## Purpose

Provide visibility into application progress.

---

## Functions

Application Status Inquiry

Milestone Tracking

Application History

Customer Status View

Operational Tracking

---

## Specification

014-application-tracking-spec.md

---

# Capability 13 – Acquisition Audit Trail

## Purpose

Provide end-to-end acquisition auditability.

---

## Functions

Capture Business Events

Capture User Activities

Capture System Activities

Support Compliance Reporting

---

## Specification

015-acquisition-audit-spec.md

---

# Capability 14 – Compliance Orchestration

## Purpose

Enforce regulatory compliance gates at defined points in the acquisition workflow.

The capability owns compliance gate execution, consent audit records, adverse action notice generation, disclosure audit records, and compliance hold management. It does not own credit policy, decisioning, or offer pricing.

---

## Compliance Gates

Gate 1 — AML Pre-Screening

Gate 2 — FCRA Credit Pull Consent Audit

Gate 3 — Adverse Action Notice (FCRA / ECOA)

Gate 4 — TILA Disclosure Audit

Gate 5 — Pre-Funding AML Re-Check

---

## Functions

Perform AML and sanctions screening via Fraud / AML Platform

Record FCRA credit pull consent before hard pull initiation

Generate and deliver FCRA / ECOA adverse action notices on declined decisions

Record TILA disclosure audit at e-sign (APR, total of payments, finance charge, loan term)

Perform AML re-check before funding and manage compliance holds

Map Decision Platform reason codes to ECOA-compliant adverse action descriptions

Manage COMPLIANCE_HOLD application state via application-management-service

---

## Regulated Obligations

FCRA — credit pull consent, adverse action notice

ECOA / Regulation B — adverse action reason codes and delivery

TILA / Regulation Z — disclosure audit at signing

Bank Secrecy Act — AML and sanctions screening

---

## Specification

openspec/changes/compliance-orchestration/

openspec/compliance-orchestration/spec.md (planned)

---

# 7. Capability Ownership Matrix

| Capability                      | Owner                          |
| ------------------------------- | ------------------------------ |
| Application Management          | Acquisition Platform           |
| Application Intake              | Acquisition Platform           |
| Compliance Orchestration        | Acquisition Platform           |
| Identity Verification           | Identity Verification Platform |
| Fraud Assessment                | Fraud Platform                 |
| AML / Sanctions Screening       | Fraud / AML Platform           |
| Credit Evaluation               | Credit Management Platform     |
| Decision Execution              | Decision Platform              |
| Funding Execution               | Funding Platform               |
| Loan Booking                    | Core Loan Platform             |
| Offer Management                | Offer Management Platform      |
| Customer Profile                | Customer Platform              |
| Document Collection             | Acquisition Platform           |
| Underwriting Workflow           | Acquisition Platform           |
| Application Tracking            | Acquisition Platform           |
| Acquisition Audit Trail         | Acquisition Platform           |

---

# 8. Success Metrics

Application Start Rate

Application Completion Rate

Application Submission Rate

Verification Success Rate

Offer Acceptance Rate

Approval Rate

Funding Rate

Average Time To Decision

Average Time To Funding

Straight Through Processing Rate

Application Abandonment Rate

---

# 9. Non-Functional Requirements

Availability

99.95%

Maximum Internal Response Time

Less Than 2 Seconds

Security

Enterprise Standards Required

Encryption

Required

Audit Logging

Required

Observability

Required

Disaster Recovery

Required

PII Protection

Required

Data Retention

7 Years Minimum

---

# 10. Related Specifications

000-domain-boundaries-and-context-map.md

002-application-intake-spec.md

003-application-spec.md

004-identity-verification-orchestration-spec.md

005-fraud-verification-orchestration-spec.md

006-credit-evaluation-orchestration-spec.md

007-decision-orchestration-spec.md

008-offer-acceptance-spec.md

009-document-collection-spec.md

010-underwriting-spec.md

011-bank-verification-spec.md

012-funding-request-spec.md

013-notification-orchestration-spec.md

014-application-tracking-spec.md

015-acquisition-audit-spec.md

016-compliance-orchestration-spec.md
