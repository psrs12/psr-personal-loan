# architecture/000-architecture-overview.md

# Personal Loan Acquisition Platform

## Architecture Overview

Version: 1.0

Status: Draft

Owner: Enterprise Architecture

---

# 1. Purpose

This document defines the high-level architecture of the Personal Loan Acquisition Platform.

The platform enables digital personal loan acquisition by managing the customer application journey and orchestrating enterprise capabilities required for loan processing.

The platform provides:

* Customer application experience
* Application lifecycle management
* Workflow orchestration
* Enterprise service integration
* Application tracking
* Audit capability

---

# 2. Architecture Vision

The Personal Loan Acquisition Platform follows a domain-driven, cloud-native, microservice-based architecture.

The architecture emphasizes:

* Clear business ownership boundaries
* Independent service deployment
* API-first integration
* Event-driven communication
* Enterprise capability reuse
* Security by design
* Observability by default

---

# 3. Architecture Principles

## AP-001 Domain Ownership

Each business capability belongs to a clearly defined bounded context.

---

## AP-002 Enterprise Capability Reuse

Existing enterprise platforms shall be consumed instead of recreated.

Examples:

Offer Management

Credit Management

Fraud Management

Decisioning

Identity

Funding

---

## AP-003 API First

All external and internal capabilities are exposed through well-defined APIs.

---

## AP-004 Event Driven Integration

Asynchronous business events shall be used where immediate response is not required.

---

## AP-005 Database Ownership

Each service owns its data.

No direct database access between services.

---

## AP-006 Security First

Authentication, authorization, encryption, auditing, and compliance are mandatory.

---

## AP-007 Cloud Native

Services are designed for:

* Container deployment
* Horizontal scaling
* Automated recovery
* Continuous delivery

---

# 4. Logical Architecture

High-level view:

```
+------------------------------------------------+
|                Client Channels                 |
|                                                |
| Web Application                                |
| Mobile Application                             |
| Partner Channels                               |
+----------------------+-------------------------+
                       |
                       v

+------------------------------------------------+
|              API Gateway Layer                 |
+------------------------------------------------+

                       |
                       v

+------------------------------------------------+
|          Personal Loan Acquisition             |
|             Application Layer                  |
+------------------------------------------------+

 |          |            |           |
 v          v            v           v

Application  Workflow  Integration  Audit
Services     Engine    Services     Services


                       |
                       v

+------------------------------------------------+
|          Enterprise Capability Layer           |
+------------------------------------------------+

Offer Management

Enterprise Fraud

Enterprise Credit

Enterprise Decision

Identity Platform

Document Platform

Notification Platform

Funding Platform

Core Loan Platform
```

---

# 5. Application Architecture Layers

## Presentation Layer

Responsibilities:

* Customer UI
* Application forms
* Application status views
* Customer interactions

Examples:

Web UI

Mobile UI

---

## API Layer

Responsibilities:

* API exposure
* Authentication enforcement
* Request validation
* Routing

Components:

API Gateway

Backend For Frontend (Optional)

---

## Application Service Layer

Responsibilities:

* Business workflows
* Application lifecycle
* State transitions
* Orchestration

Services:

application-service

invitation-service

offer-acceptance-service

underwriting-service

---

## Integration Layer

Responsibilities:

* Enterprise API communication
* Message publishing
* Message consumption
* Transformation

Services:

identity-orchestration-service

fraud-orchestration-service

credit-orchestration-service

decision-orchestration-service

---

## Data Layer

Responsibilities:

* Service-owned persistence
* Audit history
* Application state

Pattern:

Database per service

---

# 6. Domain Architecture

The Acquisition Platform owns:

```
Application Management

Customer Journey

Application Workflow

Invitation Processing

Offer Acceptance

Document Collection

Underwriting Workflow

Funding Request

Application Tracking

Acquisition Audit
```

Enterprise platforms own:

```
Offer Management

Credit Management

Fraud Management

Decisioning

Identity Verification

Funding Execution

Loan Servicing
```

---

# 7. Service Architecture

Microservices:

```
application-service



offer-acceptance-service

identity-orchestration-service

fraud-orchestration-service

credit-orchestration-service

decision-orchestration-service

document-service

underwriting-service

bank-verification-service

funding-request-service

notification-service
```

---

# 8. Communication Patterns

## Synchronous Communication

Used for:

* Request/response operations
* Real-time validation

Examples:

Application API

Offer retrieval

Verification request

Protocol:

REST / HTTPS

---

## Asynchronous Communication

Used for:

* Long running processes
* Workflow progression
* Notifications

Examples:

ApplicationSubmitted

DecisionCompleted

FundingCompleted

Technology:

Event streaming platform

---

# 9. Integration Architecture

External integrations:

```
Acquisition Platform

       |
       |
       +---- Offer Management

       +---- Fraud Platform

       +---- Credit Management

       +---- Decision Platform

       +---- Identity Platform

       +---- Document Platform

       +---- Funding Platform

       +---- Notification Platform
```

---

# 10. Data Architecture Principles

## Ownership

Application Service owns:

Application

Application Status

Application History

Workflow State

External systems own:

Enterprise master data

---

## Data Security

Sensitive data requires:

Encryption at rest

Encryption in transit

Access control

Audit logging

---

# 11. Security Architecture Overview

Security controls:

Authentication

Authorization

API security

Service identity

Secrets management

Audit logging

Standards:

OAuth 2.0

OpenID Connect

mTLS

RBAC

---

# 12. Observability Architecture

All services provide:

Logs

Metrics

Distributed tracing

Health checks

Required fields:

Correlation ID

Application ID

Transaction ID

Service Name

Timestamp

---

# 13. Deployment Architecture

Target platform:

Containerized microservices

Deployment:

Kubernetes

CI/CD enabled

Environment separation:

DEV

QA

UAT

PRODUCTION

---

# 14. Non Functional Goals

Availability:

99.95%

Scalability:

Horizontal scaling

Performance:

API response < 2 seconds

Reliability:

Retry and resilience patterns

Security:

Enterprise compliance

---

# 15. Future Evolution

Future extensions:

AI assisted underwriting

ML based journey optimization

Real-time decision optimization

Advanced analytics

Customer personalization

---

# Related Documents

000-domain-boundaries-and-context-map.md

001-acquisition-business-capabilities.md

application-state-machine.md

001-logical-architecture.md

002-microservice-boundaries.md

003-data-architecture.md

004-api-standards.md

005-event-driven-architecture.md

006-integration-patterns.md

007-security-architecture.md

008-observability-architecture.md
