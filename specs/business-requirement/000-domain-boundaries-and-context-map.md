# 000-domain-boundaries-and-context-map.md

# Personal Loan Acquisition Platform

## Domain Boundaries and Context Map

Version: 2.0

Status: Draft

Owner: Enterprise Architecture

---

# 1. Purpose

This document defines:

* Business domains
* Bounded contexts
* Ownership boundaries
* Data ownership
* System integrations
* Context relationships
* Enterprise platform dependencies

The Personal Loan Acquisition Platform is responsible for managing the customer acquisition journey and orchestrating enterprise services required to process a personal loan application.

The platform is not responsible for offer generation, fraud assessment, credit management, identity management, or decision execution.

---

# 2. Architectural Principles

## AP-001

Enterprise platforms remain the System of Record for their respective domains.

## AP-002

The Acquisition Platform shall not duplicate enterprise business capabilities.

## AP-003

The Acquisition Platform owns only personal loan acquisition workflow and customer journey orchestration.

## AP-004

No service shall directly access another service database.

## AP-005

Data sharing shall occur through APIs or Events.

## AP-006

Every bounded context owns only the data required for acquisition processing.

## AP-007

Business rules specific to Personal Loan Acquisition may reside within the Acquisition Platform.

Enterprise business rules remain owned by enterprise platforms.

---

# 3. Enterprise Context Map

External Enterprise Platforms

---

Campaign Management Platform

Offer Management Platform

Identity Verification Platform

Fraud Platform

Credit Management Platform

Decision Platform

Authentication Platform

Document Platform

Notification Platform

Funding Platform

Core Loan Platform

---

```
           |
           |
           V
```

Personal Loan Acquisition Platform

---

# 4. Acquisition Platform Capabilities

The Acquisition Platform owns the following business capabilities:

1. Invitation To Apply

2. Application Management

3. Customer Data Collection

4. Identity Verification Orchestration

5. Fraud Verification Orchestration

6. Credit Evaluation Orchestration

7. Decision Orchestration

8. Offer Acceptance

9. Document Collection

10. Underwriting Workflow

11. Bank Verification

12. Funding Request Orchestration

13. Notification Orchestration

14. Application Tracking

15. Acquisition Audit Trail

---

# 5. Bounded Contexts

---

## Invitation To Apply Context

Purpose

Start acquisition journey using an Invitation ID.

Owns

Invitation Validation Workflow

Application Initialization

Application Prefill

Invitation Usage Tracking

Does Not Own

Campaigns

Prospects

Offers

Offer Terms

Pricing

System Of Record

Offer Management Platform

---

## Application Context

Purpose

Manage personal loan application lifecycle.

Owns

Application

Application Status

Applicant Entered Data

Application Progress

Application Audit

System Of Record

Acquisition Platform

---

## Identity Verification Orchestration Context

Purpose

Coordinate identity verification activities.

Owns

Verification Workflow

Verification Status

Verification Outcome Tracking

Product Specific Identity Rules

Does Not Own

Identity Verification

Identity Scoring

Identity Audit

Identity Models

System Of Record

Identity Verification Platform

---

## Fraud Verification Orchestration Context

Purpose

Coordinate fraud verification activities.

Owns

Fraud Workflow

Fraud Outcome Tracking

Fraud Routing

Personal Loan Fraud Rules

Application Fraud Status

Does Not Own

Fraud Assessment

Fraud Scores

Fraud Models

Fraud Decisions

Fraud Audit

System Of Record

Enterprise Fraud Platform

---

## Credit Evaluation Orchestration Context

Purpose

Coordinate credit evaluation activities.

Owns

Credit Workflow

Credit Outcome Tracking

Credit Routing

Personal Loan Credit Rules

Application Credit Status

Does Not Own

Credit Bureau Data

Credit Scores

Credit Models

Credit Assessment

Credit Audit

Credit Data Management

System Of Record

Enterprise Credit Management Platform

---

## Decision Orchestration Context

Purpose

Coordinate decision execution activities.

Owns

Decision Workflow

Decision Outcome Tracking

Application Decision Status

Decision Routing

Decision Exception Handling

Does Not Own

Decision Models

Eligibility Rules

Risk Models

Decision Strategies

Decision Execution

Decision Audit

Pricing Rules

System Of Record

Enterprise Decision Platform

---

## Offer Acceptance Context

Purpose

Present approved offers and capture customer acceptance.

Owns

Offer Acceptance

Offer Selection

Disclosure Acceptance

Acceptance Audit

Does Not Own

Offer Creation

Offer Pricing

Offer Expiration Logic

Offer Eligibility

System Of Record

Offer Management Platform

---

## Document Collection Context

Purpose

Collect and manage required documents.

Owns

Document Requests

Document Status

Document Metadata

Document Tracking

Does Not Own

Document Storage

Document Retention

Document Repository

System Of Record

Document Platform

---

## Underwriting Context

Purpose

Perform manual application review.

Owns

Work Queues

Review Activities

Conditions

Manual Decisions

Approval Workflow

System Of Record

Acquisition Platform

---

## Bank Verification Context

Purpose

Validate applicant funding account.

Owns

Verification Workflow

Verification Results

Verification Status

System Of Record

Acquisition Platform

---

## Funding Request Orchestration Context

Purpose

Request loan funding.

Owns

Funding Requests

Funding Status

Funding Tracking

Funding Workflow

Does Not Own

Funding Execution

Disbursement

Payment Processing

System Of Record

Funding Platform

---

## Notification Orchestration Context

Purpose

Initiate customer communications.

Owns

Notification Requests

Notification History

Communication Tracking

Does Not Own

Message Delivery

Templates

Email Infrastructure

SMS Infrastructure

System Of Record

Enterprise Notification Platform

---

# 6. External System Ownership Matrix

Offer Management Platform

Owns:

Offers

Invitation IDs

Offer Pricing

Prospects

Campaign Relationships

---

Enterprise Credit Management Platform

Owns:

Credit Data

Credit Bureau Integrations

Credit Models

Credit Scores

Credit Assessments

---

Enterprise Fraud Platform

Owns:

Fraud Models

Fraud Scores

Fraud Assessments

Fraud Decisions

Fraud Audit

---

Enterprise Decision Platform

Owns:

Eligibility Rules

Decision Rules

Risk Models

Pricing Decisions

Approval Decisions

Decline Decisions

---

Identity Verification Platform

Owns:

Identity Verification

Identity Risk Scores

Identity Audit

---

Funding Platform

Owns:

Funding Execution

Disbursement

Settlement

---

Core Loan Platform

Owns:

Loan Accounts

Loan Booking

Servicing Setup

Customer Loan Relationship

---

# 7. High Level Workflow

Invitation To Apply

↓

Application

↓

Identity Verification

↓

Fraud Verification

↓

Credit Evaluation

↓

Decision Request

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

# 8. Database Ownership

The Acquisition Platform owns:

Application Data

Application Status

Applicant Entered Data

Offer Acceptance Data

Underwriting Data

Funding Request Data

Acquisition Audit Data

The Acquisition Platform shall not store enterprise master data except where required for workflow processing and audit.

---

# 9. Microservice Mapping

ita-service

application-service

identity-orchestration-service

fraud-orchestration-service

credit-orchestration-service

decision-orchestration-service

offer-acceptance-service

document-collection-service

underwriting-service

bank-verification-service

funding-request-service

notification-orchestration-service

---

# 10. Future Domains (Out Of Scope)

Campaign Management

Offer Management

Credit Management

Fraud Management

Decision Management

Loan Booking

Loan Servicing

Payments

Statements

Collections

Recovery

Customer Servicing

Hardship Programs
