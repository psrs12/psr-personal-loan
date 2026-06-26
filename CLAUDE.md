# CLAUDE.md

# Personal Loan Platform - Claude Code Engineering Guidelines

## Project Overview

This repository contains a production-grade Personal Loan Acquisition Platform.

The platform supports the Personal Loan application lifecycle using:

* Domain Driven Design
* Modular Architecture
* Hexagonal Architecture
* Event Driven Architecture
* API First Development

---

# Source of Truth

The following documents define the architecture, design, and implementation standards.

Claude Code MUST review these documents before creating or modifying code.

---

# Architecture References

Location:

```
docs/architecture/
```

Reference documents:

1. Architecture Overview

```
docs/architecture/01-architecture-overview.md
```

Defines:

* System context
* Major components
* Boundaries
* Architecture principles

2. Domain Architecture

```
docs/architecture/02-domain-architecture.md
```

Defines:

* Bounded contexts
* Domain ownership
* Capability boundaries

3. Application Management Architecture

```
docs/architecture/03-application-management.md
```

Defines:

* Application Management capability
* Module responsibilities
* Dependencies

4. Integration Architecture

```
docs/architecture/04-integration-architecture.md
```

Defines:

* External systems
* API integrations
* Adapter patterns

5. Data Architecture

```
docs/architecture/05-data-architecture.md
```

Defines:

* Persistence strategy
* Data ownership
* Storage patterns

6. Security Architecture

```
docs/architecture/06-security-architecture.md
```

Defines:

* Authentication
* Authorization
* PII handling

7. Event Architecture

```
docs/architecture/07-event-architecture.md
```

Defines:

* Domain events
* Messaging patterns
* Kafka usage

8. Deployment Architecture

```
docs/architecture/08-deployment-architecture.md
```

Defines:

* Runtime environment
* Containerization
* Deployment model

9. Observability Architecture

```
docs/architecture/09-observability-architecture.md
```

Defines:

* Logging
* Metrics
* Tracing

10. Non Functional Requirements

```
docs/architecture/10-nfr.md
```

Defines:

* Performance
* Availability
* Scalability
* Reliability

---

# Design References

Location:

```
docs/design/
```

Reference documents:

1. Domain Design

```
docs/design/01-domain-design.md
```

Contains:

* Aggregates
* Entities
* Value Objects
* Domain rules

2. Application Design

```
docs/design/02-application-design.md
```

Contains:

* Use cases
* Application services
* Workflow orchestration

3. API Design

```
docs/design/03-api-design.md
```

Contains:

* REST standards
* API contracts
* Error handling

4. Database Design

```
docs/design/04-database-design.md
```

Contains:

* Schema
* Tables
* Relationships
* Migration strategy

5. Integration Design

```
docs/design/05-integration-design.md
```

Contains:

* External clients
* Resilience
* Timeout
* Retry patterns

6. Testing Design

```
docs/design/06-testing-design.md
```

Contains:

* Unit testing
* Integration testing
* Contract testing

---

# OpenSpec References

Business capability specifications are stored under:

```
openspec/
```

Current capability:

```
openspec/application-management/
```

Documents:

```
01-capability.md
02-domain-model.md
03-invitation-processing.md
04-sequence-diagrams.md
05-api-contracts.md
06-persistence-model.md
07-acceptance-tests.md
```

OpenSpec defines:

* Scope
* Business rules
* Domain behavior
* API contracts
* Acceptance criteria

---

# Development Rules

## Scope Control

Before implementation:

1. Read relevant OpenSpec.
2. Read related architecture documents.
3. Read related design documents.
4. Confirm ownership boundary.

Do not expand scope.

---

# Architecture Rules

Follow existing architecture.

Do not:

* Create unnecessary microservices
* Create new bounded contexts without approval
* Duplicate external capabilities
* Move ownership across domains

Prefer:

* Modular design
* Clear boundaries
* Reusable components inside the domain

---

# Coding Rules

Generate:

* Clean code
* Constructor injection
* Immutable objects where possible
* Small focused classes
* Domain-driven naming

Avoid:

* God classes
* Static utilities
* Tight coupling
* Business logic in controllers

---

# Layer Rules

## Domain Layer

Contains:

* Aggregates
* Entities
* Value Objects
* Domain Services

Must not contain:

* REST
* Database
* External APIs

---

## Application Layer

Contains:

* Use cases
* Workflow orchestration
* Transaction boundaries

---

## Infrastructure Layer

Contains:

* Database adapters
* External API clients
* Messaging

---

## API Layer

Contains:

* Controllers
* Request/Response models
* Validation

Controllers remain thin.

---

# Integration Rules

External systems must use adapters.

Never call external systems directly from:

* Controllers
* Domain objects

---

# Testing Rules

Every feature requires:

* Unit tests
* Integration tests
* Contract tests

Use:

* JUnit 5
* Mockito
* Testcontainers

---

# Security Rules

Assume production banking environment.

Protect:

* Customer information
* PII
* Financial data

Never log sensitive information.

---

# Git Rules

Branches:

```
feature/<ticket>-<description>
```

Commit style:

```
type: description
```

Examples:

```
feat: add invitation validation

test: add invitation scenarios

docs: update api contract
```

---

# Claude Code Execution Flow

For every change:

1. Review OpenSpec
2. Review Architecture
3. Review Design
4. Propose implementation approach
5. Generate code
6. Generate tests
7. Validate against acceptance criteria

The objective is production-quality enterprise software.
