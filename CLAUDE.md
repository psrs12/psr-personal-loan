# CLAUDE.md

# Personal Loan Platform - Claude Code Engineering Guidelines

## Project Overview

This repository contains a production-grade Personal Loan Acquisition Platform.

The platform supports the full Personal Loan application lifecycle using:

* Domain Driven Design
* Hexagonal Architecture (ports and adapters)
* Event Driven Architecture (Apache Kafka — choreography)
* API First Development
* Micro-Frontend Architecture (applicant self-service portal)

---

# Services

**Backend**

| Service | Port | Responsibility |
|---------|------|----------------|
| `invitation-service` | 8080 | Invitation token generation and validation |
| `application-management-service` | 8081 | Application lifecycle, state machine, applicant login, event timeline |
| `pricing-orchestration-service` | 8082 | Soft pull, offer pricing, hard pull, final decision routing |
| `offer-acceptance-service` | 8085 | Declarations, e-signature, ESignCompleted event |
| `document-service` | 8084 | Document requirements, upload, virus scan lifecycle, completion detection |

**Frontend**

| UI | Responsibility |
|----|----------------|
| `application-management-ui` | Micro-frontend shell — ITA flow web components + applicant self-service portal (login, state routing, embeds post-decision web components) |
| `pricing-offers-ui` | Standalone `<pricing-offer-selector>` web component — offer list, offer selection, hard pull consent, fires `offer-confirmed` custom event |
| `document-management-ui` | Standalone `<document-upload-manager>` web component — document requirements list, per-requirement file upload, progress bar, status tracking |

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

| File | Purpose |
|------|---------|
| `000-architecture-overview.md` | System context, service landscape, event topology, security summary |
| `000-domain-boundaries-and-context-map.md` | Bounded contexts, ownership boundaries, enterprise platform dependencies |
| `001-acquisition-business-capabilities.md` | Business capability inventory |
| `001-logical-architecture.md` | Logical layering and component relationships |
| `002-application-state-machine.md` | **All application states, valid transitions, event ownership** — read before touching any state logic |
| `002-microservice-boundaries.md` | Service decomposition strategy and data ownership rules |
| `003-data-architecture.md` | Persistence strategy, data ownership |
| `004-api-standards.md` | REST conventions, versioning, error handling |
| `005-event-driven-architecture.md` | Kafka patterns, event ownership, consumer rules |
| `006-integration-patterns.md` | Adapter patterns, ACL pattern, resilience |
| `007-security-architecture.md` | Authentication, authorisation, PII handling |
| `008-observability-architecture.md` | Logging, metrics, tracing, required fields |
| `009-deployment-architecture.md` | Containerisation, Kubernetes, environment model |
| `010-non-functional-requirements.md` | Performance, availability, scalability targets |

---

# Flow References

Location:

```
docs/flows/
```

| File | Purpose |
|------|---------|
| `01-functional-flow.md` | End-to-end business journey — all phases from invitation to funding. Read for business context before implementing any phase. |
| `02-component-interaction-flows.md` | Sequence diagrams for all major service interactions and event flows |
| `03-scenario-flows.md` | Step-by-step scenario walkthroughs: approved, documents required, declined, login, virus rejection, idempotency |

---

# Design References

Location:

```
docs/design/
```

| File | Purpose |
|------|---------|
| `001-service-design-guidelines.md` | Hexagonal architecture patterns within each service |
| `002-database-design-guidelines.md` | Schema ownership, migration strategy |
| `005-testing-strategy.md` | Unit, integration, contract testing approach |

---

# Standards References

Location:

```
docs/standards/
```

| File | Purpose |
|------|---------|
| `003-error-handling-standard.md` | ProblemDetail format, HTTP status codes, error codes |
| `004-logging-standard.md` | Structured logging, required fields, PII rules |
| `006-coding-standard.md` | Code conventions, layer rules |

---

# OpenSpec References

Business capability specifications are stored under:

```
openspec/
```

| Location | Status | Purpose |
|----------|--------|---------|
| `openspec/application-management/` | Active | Core application management capability spec |
| `openspec/changes/post-decision-flow/` | Active | Post-decision flows: offer acceptance, document collection, applicant login |

Each change directory contains:

```
proposal.md       — what and why
design.md         — how
tasks.md          — implementation steps with completion tracking
specs/            — per-capability detailed specs
```

OpenSpec defines:

* Scope and business rules
* Domain behaviour and acceptance criteria
* API contracts
* Event contracts

---

# Key Domain Rules

## Application State Machine

`application-management-service` is the **sole owner** of application state. No other service may directly write application status except through:

1. `PATCH /applications/{id}/status` REST call
2. Kafka event consumption by registered consumers

Read `docs/architecture/002-application-state-machine.md` before any state-related work.

Current states: `CREATED → STARTED → IN_PROGRESS → SUBMITTED → PROCESSING → APPROVED | DECLINED | REFERRED | DOCUMENTS_REQUIRED → UNDERWRITING → OFFER_ACCEPTED → FUNDING_PENDING → FUNDED → COMPLETED`

Terminal states: `DECLINED`, `CANCELLED`, `EXPIRED`, `COMPLETED`

## Event Contract

The `FinalDecisionDocumentsRequired` event contract between the Decision Engine and `document-service` is versioned at:

```
docs/contracts/decision-engine-events/v1/documents-required.json
```

Do not change document type codes without updating this contract.

## Anti-Corruption Layer

`document-service` owns the ACL mapping from Decision Engine document type codes to platform `DocumentType` domain values. The mapping lives in `DecisionEngineDocumentCodeMapper`. Do not add Decision Engine vocabulary to any other service.

## ITA Prefill Rules

In the ITA flow, **name and address are prefilled from the invitation and are read-only** — the applicant cannot edit them. Only non-identity fields (employment, income, financial obligations) are editable.

## Applicant Login

Applicant self-service login is `POST /applications/login` (public endpoint — no auth header). Returns a JJWT session token (30-minute expiry). Login is rejected for terminal application states.

Verification is currently handled by `StubVerificationAdapter` (feature flag: `verification.stub.enabled=true`). Production implementation will replace this via `VerificationPort`.

---

# Development Rules

## Scope Control

Before implementation:

1. Read the relevant OpenSpec (`openspec/` directory).
2. Read related architecture documents (`docs/architecture/`).
3. Read relevant flow documents (`docs/flows/`).
4. Confirm ownership boundary — do not implement logic that belongs to another service.
5. Do not expand scope beyond the task.

## Architecture Rules

Follow existing architecture. Do not:

* Create unnecessary microservices
* Create new bounded contexts without approval
* Duplicate enterprise platform capabilities (credit, fraud, identity, decisioning, funding)
* Move data ownership across service boundaries

Prefer:

* Modular design within the existing service landscape
* Clear port/adapter boundaries per hexagonal architecture
* ACL adapters when integrating with enterprise systems

---

# Coding Rules

Generate:

* Clean code with constructor injection
* Immutable objects where possible (records for value objects, DTOs)
* Small, focused classes — one responsibility per class
* Domain-driven naming aligned to the ubiquitous language

Avoid:

* God classes
* Static utilities
* Tight coupling between layers
* Business logic in controllers or infrastructure classes

---

# Layer Rules

## Domain Layer

Package: `domain/`

Contains: Aggregates, Entities, Value Objects, Domain Services, Port interfaces, Domain Events, Domain Exceptions

Must not contain: REST, database, Kafka, or any infrastructure concern.

## Application Layer

Package: `application/`

Contains: Use cases (`@Service`), workflow orchestration, transaction boundaries (`@Transactional`)

One use case class per business operation. Use cases call domain objects and port interfaces only.

## Infrastructure Layer

Package: `infrastructure/`

Contains: JPA entities and repositories, Kafka consumers and producers, REST client adapters, storage adapters

Implements port interfaces defined in the domain layer.

## API Layer

Package: `api/`

Contains: Controllers (`@RestController`), request/response records, `@Valid` validation, `@RestControllerAdvice` exception handlers

Controllers are thin — they delegate immediately to use cases. No business logic in controllers.

---

# Integration Rules

Enterprise systems (Decision Engine, Credit, Fraud, Identity, Offer, Funding platforms) must be accessed through port interfaces and infrastructure adapters.

Never call external systems directly from controllers or domain objects.

Use ACL adapters when the external system's model differs from the platform domain model.

---

# Testing Rules

Every feature requires:

* Unit tests — domain logic, use cases (Mockito mocks for ports)
* Integration tests — full slice with Testcontainers (PostgreSQL + Kafka)
* Acceptance tests — API-level scenarios validating acceptance criteria from OpenSpec

Use:

* JUnit 5
* Mockito
* Testcontainers
* WireMock (for external REST dependencies)
* `@SpringBootTest` + `MockMvc` for acceptance tests

---

# Security Rules

Assume production banking environment.

* Never log SSN, full account numbers, or any PII
* SSN is stored as a token — never in plaintext
* All post-login endpoints require `Authorization: Bearer <token>` header
* Session tokens expire after 30 minutes
* `POST /applications/login` is the only public (unauthenticated) endpoint on application-management-service

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

Types: `feat`, `fix`, `test`, `docs`, `refactor`, `chore`

Examples:

```
feat: add FinalDecisionDocumentsRequired consumer
test: add upload validation unit tests
docs: update application state machine spec
```

---

# Claude Code Execution Flow

For every change:

1. Read the relevant OpenSpec (`openspec/changes/<change>/`)
2. Read related architecture and flow documents
3. Confirm service ownership and layer placement
4. Propose implementation approach if non-trivial
5. Implement code following hexagonal layer rules
6. Write unit tests and acceptance tests
7. Update task checklist in `tasks.md`
8. Validate against acceptance criteria in OpenSpec

The objective is production-quality enterprise software that a senior engineer would be proud to ship.
