# 001-acquisition-business-capabilities.md

# Personal Loan Acquisition Platform

## Business Capability Model

Version: 2.0

Status: Draft

Owner: Enterprise Architecture

---

# 1. Purpose

This document defines the business capabilities owned by the Personal Loan Acquisition Platform.

The platform provides the end-to-end digital acquisition journey for personal loan applicants and orchestrates enterprise services required to evaluate, approve, and fund a loan application.

The platform does not own enterprise capabilities such as Offer Management, Identity Verification, Fraud Management, Credit Management, Decisioning, Notification Delivery, or Funding Execution.

---

# 2. Business Objectives

The platform shall:

* Provide a seamless digital loan application experience.
* Support invitation-based acquisition journeys.
* Support direct application journeys.
* Coordinate enterprise verification and decision services.
* Reduce application abandonment.
* Support straight-through processing.
* Support manual underwriting when required.
* Support loan funding initiation.
* Maintain complete acquisition auditability.

---

# 3. Business Scope

## In Scope

Invitation To Apply

Application Management

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

Credit Management Platform

Decision Platform

Document Platform

Notification Platform

Funding Platform

Core Loan Platform

Authentication Platform

---

# 5. Acquisition Journey

The acquisition journey consists of the following stages.

Invitation To Apply

↓

Application Initiation

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

↓

Application Completed

---

# 6. Business Capabilities

## Capability 1 – Invitation To Apply

Purpose

Allow applicants to initiate applications using an Invitation ID.

Functions

Validate Invitation

Retrieve Offer Information

Retrieve Prospect Information

Prefill Application

Initialize Application

Specification

002-invitation-to-apply-spec.md

---

## Capability 2 – Application Management

Purpose

Manage application lifecycle.

Functions

Create Application

Save Application

Resume Application

Submit Application

Track Application Status

Manage Applicant Information

Specification

003-application-spec.md

---

## Capability 3 – Identity Verification Orchestration

Purpose

Coordinate identity verification activities.

Functions

Initiate Verification

Track Verification Status

Process Verification Results

Apply Product-Specific Verification Rules

Specification

004-identity-verification-orchestration-spec.md

---

## Capability 4 – Fraud Verification Orchestration

Purpose

Coordinate fraud evaluation activities.

Functions

Initiate Fraud Evaluation

Track Fraud Status

Process Fraud Results

Apply Personal Loan Fraud Policies

Route Fraud Exceptions

Specification

005-fraud-verification-orchestration-spec.md

---

## Capability 5 – Credit Evaluation Orchestration

Purpose

Coordinate credit evaluation activities.

Functions

Initiate Credit Evaluation

Track Credit Status

Process Credit Results

Apply Personal Loan Credit Policies

Route Credit Exceptions

Specification

006-credit-evaluation-orchestration-spec.md

---

## Capability 6 – Decision Orchestration

Purpose

Coordinate decision processing.

Functions

Submit Decision Requests

Track Decision Status

Process Decision Outcomes

Route Referred Applications

Manage Decision Exceptions

Specification

007-decision-orchestration-spec.md

---

## Capability 7 – Offer Acceptance

Purpose

Present offers and capture acceptance.

Functions

Display Approved Offers

Capture Acceptance

Capture Disclosures

Capture Electronic Consent

Specification

008-offer-acceptance-spec.md

---

## Capability 8 – Document Collection

Purpose

Collect required applicant documents.

Functions

Generate Document Requests

Upload Documents

Track Document Status

Manage Outstanding Requirements

Specification

009-document-collection-spec.md

---

## Capability 9 – Underwriting Workflow

Purpose

Support manual application review.

Functions

Create Work Queues

Assign Reviews

Manage Conditions

Capture Review Decisions

Track Underwriting Status

Specification

010-underwriting-spec.md

---

## Capability 10 – Bank Verification

Purpose

Verify applicant funding accounts.

Functions

Initiate Verification

Track Verification Results

Manage Verification Exceptions

Specification

011-bank-verification-spec.md

---

## Capability 11 – Funding Request Orchestration

Purpose

Initiate loan funding.

Functions

Create Funding Requests

Track Funding Status

Process Funding Responses

Manage Funding Exceptions

Specification

012-funding-request-spec.md

---

## Capability 12 – Notification Orchestration

Purpose

Initiate applicant communications.

Functions

Generate Notification Requests

Track Notification Status

Manage Communication History

Specification

013-notification-orchestration-spec.md

---

## Capability 13 – Application Tracking

Purpose

Provide visibility into application progress.

Functions

Status Inquiry

Milestone Tracking

Application History

Customer Status View

Operational Tracking

Specification

014-application-tracking-spec.md

---

## Capability 14 – Acquisition Audit Trail

Purpose

Provide end-to-end auditability.

Functions

Capture Business Events

Capture User Activities

Capture System Activities

Support Compliance Reporting

Specification

015-acquisition-audit-spec.md

---

# 7. Capability Ownership Matrix

Capability                                 Owner

Invitation Management                      Offer Management Platform

Offer Management                           Offer Management Platform

Identity Verification                      Identity Verification Platform

Fraud Assessment                           Fraud Platform

Credit Evaluation                          Credit Management Platform

Decision Execution                         Decision Platform

Funding Execution                          Funding Platform

Loan Booking                               Core Loan Platform

---

Application Management                     Acquisition Platform

Offer Acceptance                           Acquisition Platform

Document Collection                        Acquisition Platform

Underwriting Workflow                      Acquisition Platform

Funding Request Orchestration              Acquisition Platform

Application Tracking                       Acquisition Platform

Acquisition Audit Trail                    Acquisition Platform

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

002-invitation-to-apply-spec.md

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
