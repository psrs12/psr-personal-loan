# acquisition/invitation-to-apply/010-invitation-to-apply-implementation-plan.md

# Personal Loan Acquisition Platform

# Invitation To Apply Implementation Plan

Version: 1.0

Status: Draft

Owner: Acquisition Domain

---

# 1. Purpose

This document defines the implementation roadmap for building the Invitation To Apply (ITA) microservice.

The goal is to deliver a production-ready enterprise service following:

* Domain Driven Design
* Microservice architecture
* Cloud-native practices
* Enterprise security standards

---

# 2. Implementation Approach

Development will follow:

```text
Design

↓

Develop

↓

Test

↓

Integrate

↓

Deploy

↓

Operate
```

---

# 3. Technology Stack

## Backend

Language:

```text
Java 21
```

Framework:

```text
Spring Boot 3.x
```

---

## API

```text
REST
OpenAPI 3.0
```

---

## Database

Recommended:

```text
PostgreSQL
```

---

## Persistence

```text
Spring Data JPA

Hibernate
```

---

## Messaging

```text
Apache Kafka
```

---

## Security

```text
OAuth2

Spring Security
```

---

## Testing

```text
JUnit 5

Mockito

Testcontainers

WireMock
```

---

# 4. Repository Structure

Recommended:

```text
personal-loan-acquisition-ita

├── README.md
├── pom.xml
│
├── src
│   |
│   ├── main
│   │
│   │── java
│   │
│   └── resources
│
├── api
│
├── domain
│
├── application
│
├── infrastructure
│
├── test
│
├── docker
│
├── helm
│
└── docs
```

---

# 5. Package Structure

```text
com.company.loan.acquisition.ita


├── api
│
│   ├── controller
│   ├── dto
│   └── mapper
│
├── application
│
│   ├── service
│   └── usecase
│
├── domain
│
│   ├── entity
│   ├── valueobject
│   ├── exception
│   └── service
│
├── infrastructure
│
│   ├── persistence
│   ├── client
│   ├── messaging
│   └── security
│
└── config
```

---

# 6. Development Phases

---

# Phase 1: Project Bootstrap

Duration:

1 Sprint

---

Tasks:

## Repository Creation

Create:

* Git repository
* Branch strategy
* Build pipeline

---

## Application Skeleton

Create:

* Spring Boot project
* Base packages
* Configuration

---

## Developer Environment

Provide:

Docker Compose:

```text
PostgreSQL

Kafka

Redis (optional)
```

---

Deliverables:

☐ Build successful

☐ Application starts

☐ Health endpoint available

---

# Phase 2: Domain Implementation

Duration:

1 Sprint

---

Tasks:

Implement:

## Entities

```text
Invitation

CustomerSnapshot

ValidationHistory
```

---

## Value Objects

```text
InvitationId

OfferId

ApplicationId
```

---

## State Machine

Implement:

```text
CREATED

VALIDATED

READY_TO_APPLY

APPLICATION_CREATED

EXPIRED

REJECTED
```

---

Deliverables:

☐ Domain tests completed

☐ State transitions validated

---

# Phase 3: Database Layer

Duration:

1 Sprint

---

Tasks:

Create:

Tables:

```text
invitation

validation_history

customer_snapshot

audit
```

---

Implement:

Repositories:

```java
InvitationRepository

ValidationRepository
```

---

Database migration:

Using:

```text
Flyway
```

---

Deliverables:

☐ Schema created

☐ CRUD operations complete

---

# Phase 4: API Development

Duration:

1 Sprint

---

Implement:

## Validate Invitation

```http
POST /invitations/validate
```

---

## Retrieve Offer

```http
GET /invitations/{id}/offer
```

---

## Applicant Prefill

```http
GET /invitations/{id}/applicant
```

---

## Start Application

```http
POST /applications/start
```

---

Deliverables:

☐ OpenAPI published

☐ API tests complete

---

# Phase 5: Offer Management Integration

Duration:

1 Sprint

---

Tasks:

Create adapter:

```text
OfferManagementClient
```

---

Implement:

* OAuth2 client
* REST client
* Timeout
* Retry
* Circuit breaker

---

Libraries:

```text
Spring Cloud OpenFeign

Resilience4j
```

---

Deliverables:

☐ External API integrated

☐ Failure handling complete

---

# Phase 6: Event Implementation

Duration:

1 Sprint

---

Implement Kafka producer.

Events:

```text
InvitationValidated

OfferRetrieved

ApplicationCreated

InvitationExpired
```

---

Tasks:

* Schema creation
* Topic configuration
* Producer testing

---

Deliverables:

☐ Events published

☐ Schema validated

---

# Phase 7: Security Implementation

Duration:

1 Sprint

---

Implement:

OAuth2 Resource Server

---

Security:

JWT validation

---

Authorization:

Roles:

```text
PROSPECT

SYSTEM_CLIENT

INTERNAL_SERVICE
```

---

Deliverables:

☐ APIs secured

☐ Unauthorized access blocked

---

# Phase 8: Observability

Duration:

1 Sprint

---

Implement:

## Logging

Structured JSON logs

Fields:

```text
correlationId

traceId

invitationId

applicationId
```

---

## Metrics

Expose:

```text
/actuator/prometheus
```

---

Metrics:

```text
invitation.validation.count

offer.lookup.time

application.created.count
```

---

## Distributed Tracing

Implement:

OpenTelemetry

---

Deliverables:

☐ Logs searchable

☐ Metrics available

☐ Trace available

---

# Phase 9: Testing

Duration:

1 Sprint

---

Automation:

## Unit Tests

Coverage:

> 85%

---

## Integration Tests

Using:

Testcontainers

---

## Contract Tests

Using:

Pact

---

## Performance Tests

Using:

Gatling

---

Deliverables:

☐ Regression suite

☐ Quality gates

---

# Phase 10: Deployment

Duration:

1 Sprint

---

Container:

Docker image

---

Example:

```text
ita-service:1.0
```

---

Kubernetes:

Deployment

Service

ConfigMap

Secret

---

Helm:

```text
charts/ita-service
```

---

Deliverables:

☐ Deployable artifact

☐ Rollback supported

---

# 7. CI/CD Pipeline

Pipeline:

```text
Commit

↓

Build

↓

Unit Test

↓

Security Scan

↓

Container Build

↓

Integration Test

↓

Deploy DEV

↓

Deploy QA

↓

Production Approval
```

---

# 8. Definition Of Done

Feature complete when:

☐ Code implemented

☐ Unit tests complete

☐ API documented

☐ Security enabled

☐ Logging enabled

☐ Metrics available

☐ Integration tested

☐ Deployment validated

---

# 9. Production Readiness Checklist

## Application

☐ Health checks

☐ Graceful shutdown

☐ Configuration externalized

---

## Security

☐ OAuth enabled

☐ Secrets vaulted

☐ Audit enabled

---

## Reliability

☐ Retry configured

☐ Circuit breaker configured

☐ Idempotency implemented

---

## Operations

☐ Dashboard created

☐ Alerts configured

☐ Runbook created

---

# 10. Suggested Sprint Breakdown

| Sprint | Deliverable       |
| ------ | ----------------- |
| 1      | Project bootstrap |
| 2      | Domain model      |
| 3      | Database          |
| 4      | APIs              |
| 5      | Offer integration |
| 6      | Events            |
| 7      | Security          |
| 8      | Observability     |
| 9      | Testing           |
| 10     | Deployment        |

---

# 11. Future Enhancements

Future versions may add:

* Customer authentication integration
* Application workflow orchestration
* Decision platform integration
* Fraud verification integration
* Credit assessment integration

---

# Related Documents

* 001-invitation-to-apply-spec.md
* 002-state-machine.md
* 003-api-spec.md
* 004-data-model.md
* 005-integration-spec.md
* 006-service-design.md
* 007-openapi.yaml
* 008-event-spec.md
