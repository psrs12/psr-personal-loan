# Personal Loan Acquisition Platform

## Architecture Overview

Version: 2.0
Status: Current
Owner: Enterprise Architecture

---

# 1. Purpose

This document defines the high-level architecture of the Personal Loan Acquisition Platform.

The platform enables digital personal loan acquisition by managing the end-to-end customer application journey and orchestrating enterprise capabilities required for loan processing.

The platform provides:

- Invitation-to-Apply (ITA) customer entry flow
- Application lifecycle management
- Identity, fraud, and credit orchestration
- Pricing and offer presentation
- Offer acceptance with e-signature
- Document collection workflow
- Underwriting state management
- Post-decision applicant self-service portal
- Application event timeline for agent support
- Audit capability

---

# 2. Architecture Vision

The Personal Loan Acquisition Platform follows a domain-driven, cloud-native microservice architecture built on:

- Domain-Driven Design (DDD) with bounded contexts
- Hexagonal Architecture (ports and adapters) within each service
- Event-Driven Architecture (EDA) using Apache Kafka for choreography
- API-First development with REST contracts
- Micro-frontend architecture for the applicant-facing UI shell

---

# 3. Architecture Principles

| ID | Principle | Statement |
|----|-----------|-----------|
| AP-001 | Domain Ownership | Each business capability belongs to a clearly defined bounded context with a single owning service |
| AP-002 | Enterprise Capability Reuse | Enterprise platforms (Offer, Credit, Fraud, Decision, Identity, Funding) are integrated, never duplicated |
| AP-003 | API First | All capabilities are exposed through versioned, contract-first REST APIs |
| AP-004 | Event-Driven Integration | Asynchronous Kafka events are used for workflow choreography wherever immediate response is not required |
| AP-005 | Database Ownership | Each service owns its schema. No cross-service database access is permitted |
| AP-006 | Security First | Authentication, authorization, PII protection, and audit are mandatory at every layer |
| AP-007 | Cloud Native | Services are containerised, horizontally scalable, and continuously deliverable |
| AP-008 | Anti-Corruption Layer | Integration with enterprise systems uses ACL adapters to preserve domain model integrity |

---

# 4. System Context

```
┌─────────────────────────────────────────────────────────────────┐
│                        External Actors                          │
│                                                                 │
│  Applicant (Web/Mobile)   Call Centre Agent   Partner Channel   │
└────────────────────────────────┬────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│               Personal Loan Acquisition Platform                │
│                                                                 │
│  Frontend                                                       │
│  ├── application-management-ui (Micro-Frontend Shell)           │
│  │   ├── Applicant Portal (Login, Status, Post-Decision MFEs)   │
│  │   └── ITA Flow (Invitation-to-Apply, Application Form)       │
│  ├── pricing-offers-ui                                          │
│  │   └── <pricing-offer-selector> web component                 │
│  │       (offer list, offer selection, hard pull consent)        │
│  └── document-management-ui                                     │
│      └── <document-upload-manager> web component                │
│          (requirements display, file upload, status tracking)    │
│                                                                 │
│  Backend Services                                               │
│  ├── invitation-service                                         │
│  ├── application-management-service                             │
│  ├── pricing-orchestration-service                              │
│  ├── offer-acceptance-service                                   │
│  └── document-service                                           │
└────────────────────────────────┬────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Enterprise Capability Layer                   │
│                                                                 │
│  Offer Management Platform                                      │
│  Credit Management Platform                                     │
│  Fraud Management Platform                                      │
│  Decision Engine Platform                                       │
│  Identity Platform                                              │
│  Funding Platform                                               │
│  Notification Platform                                          │
└─────────────────────────────────────────────────────────────────┘
```

---

# 5. Service Landscape

## 5.1 Acquisition Services (Platform-Owned)

| Service | Port | Responsibility |
|---------|------|----------------|
| `invitation-service` | 8080 | Generates and validates invitation tokens (ITA flow) |
| `application-management-service` | 8081 | Application lifecycle, state machine, applicant login, event timeline |
| `pricing-orchestration-service` | 8082 | Soft pull, pricing, offer retrieval, hard pull, final decision routing |
| `offer-acceptance-service` | 8085 | Declarations management, e-signature capture, ESignCompleted event |
| `document-service` | 8084 | Document requirement tracking, upload handling, virus scan lifecycle, completion detection |

## 5.2 UI Layer

| Component | Type | Responsibility |
|-----------|------|----------------|
| `application-management-ui` | Micro-frontend shell | Owns ITA flow web components and the applicant self-service portal (login → state routing → embeds post-decision web components) |
| `pricing-offers-ui` | Standalone web component | Publishes `<pricing-offer-selector>` — renders available offers, captures offer selection, presents hard pull consent, fires `offer-confirmed` custom event |
| `document-management-ui` | Standalone web component | Publishes `<document-upload-manager>` — renders document requirements, handles file upload per requirement, tracks upload and virus scan status, shows progress |

## 5.3 Enterprise Platform Dependencies

| Platform | Integration Pattern | Owned By |
|----------|--------------------|-|
| Offer Management | REST (synchronous) | Enterprise |
| Credit / Soft Pull | REST via pricing-orchestration | Enterprise |
| Fraud Platform | REST via pricing-orchestration | Enterprise |
| Decision Engine | REST + Kafka events | Enterprise |
| Identity Platform | REST (verification-service, planned) | Enterprise |
| Funding Platform | REST (funding-request-service, planned) | Enterprise |

---

# 6. Application State Machine (Current)

```
CREATED → STARTED → IN_PROGRESS → SUBMITTED → PROCESSING
                                                    │
                              ┌─────────────────────┤
                              │                     │
                              ▼                     ▼
                          DECLINED             APPROVED
                         (terminal)                │
                                                   ▼
                                          DOCUMENTS_REQUIRED ──► UNDERWRITING
                                                   │                   │
                                          OFFER_ACCEPTED ◄─────────────┘
                                                   │
                                          FUNDING_PENDING
                                                   │
                                               FUNDED
                                                   │
                                            COMPLETED (terminal)
```

**State ownership:** `application-management-service` owns all state transitions.
State changes are triggered by domain events consumed from Kafka (ESignCompleted, DocumentsCompleted) or by direct API calls from orchestration services.

See `002-application-state-machine.md` for full transition table.

---

# 7. Event Architecture Summary

The platform uses Apache Kafka as the event streaming backbone. Services choreograph workflow through domain events — no central orchestrator exists on the backend.

## Key Topics

| Topic | Producer | Consumers |
|-------|----------|-----------|
| `pricing-events` | pricing-orchestration-service | offer-acceptance-service, document-service |
| `offer-acceptance-events` | offer-acceptance-service | application-management-service |
| `document-events` | document-service | application-management-service |
| `application-events` | application-management-service | notification-service (planned), audit-service |

## Key Event Contracts

| Event | Topic | Schema Location |
|-------|-------|----------------|
| `FinalDecisionApproved` | pricing-events | Inline JSON map |
| `FinalDecisionDocumentsRequired` | pricing-events | `docs/contracts/decision-engine-events/v1/documents-required.json` |
| `ESignCompleted` | offer-acceptance-events | Domain record |
| `DocumentsCompleted` | document-events | Domain record |

---

# 8. Integration Patterns

## 8.1 Synchronous (REST)

Used for: request/response operations, real-time queries, state updates.

Examples:
- Applicant login (`POST /applications/login`)
- Document upload (`POST /applications/{id}/documents/upload`)
- E-sign submission (`POST /applications/{id}/esign`)
- Status update from orchestration services to application-management-service

## 8.2 Asynchronous (Kafka)

Used for: workflow progression, state transitions, cross-service notification.

Pattern: Choreography — each service reacts to events from other services without being called directly.

## 8.3 Anti-Corruption Layer (ACL)

Used when integrating with enterprise systems that use different naming or domain models.

Example: `DecisionEngineDocumentCodeMapper` in document-service translates Decision Engine codes (e.g. `BANK_STMT_3M`) into the platform's `DocumentType` domain model.

---

# 9. UI Architecture

The platform has two independently deployable frontend units.

## application-management-ui

Vite/React micro-frontend shell. Owns the ITA application flow and the post-decision applicant portal.

```
application-management-ui
├── ITA Flow (Web Components)
│   ├── ita-traditional-form    (standalone web component)
│   └── ita-progressive-form    (standalone web component)
│
└── Applicant Self-Service Portal
    ├── ApplicantLoginPage           (applicationId + last4SSN + DOB)
    ├── ApplicantShell               (3-second status poll → MFE router)
    ├── OfferAcceptanceMfe           (declarations + e-sign)
    ├── <document-upload-manager>    (embedded web component from document-management-ui)
    ├── DenialMfe                    (adverse action information)
    └── ConfirmationMfe              (post-sign / funded confirmation)
```

The shell polls `GET /applications/{id}` every 3 seconds and routes to the appropriate MFE based on `applicationStatus`. Session token from login is forwarded as `Authorization: Bearer <token>` on all authenticated calls. The `<document-upload-manager>` web component is embedded directly — it is owned and deployed by `document-management-ui`, not the shell.

## pricing-offers-ui

Vite/React application that builds and exports the custom element `<pricing-offer-selector>`.

```
pricing-offers-ui
└── <pricing-offer-selector> (custom element / web component)
    ├── OfferFlow               (top-level state machine: loading → offer-list → consent → submitted)
    ├── OfferList               (renders available pricing offers)
    ├── OfferRow                (individual offer card with selection)
    └── ConsentStep             (hard pull disclosure + confirmation)
```

**Attributes:** `api-base-url`, `application-id`, `applicant-reference`

**Events emitted:**
- `offer-confirmed` — fired when the applicant confirms offer selection and hard pull consent; carries `{ offerId }` in `event.detail`
- `offer-error` — fired on unrecoverable errors; carries `{ error }` in `event.detail`

The host page listens for `offer-confirmed` to proceed to the decision phase.

## document-management-ui

Vite/React application that builds and exports the custom element `<document-upload-manager>`.

```
document-management-ui
└── <document-upload-manager> (custom element / web component)
    ├── DocumentManager         (top-level — fetches requirements, renders list with progress bar)
    ├── RequirementCard         (per-requirement card — file input, upload button, status badge)
    └── StatusBadge             (PENDING / UPLOADED / SCANNING / VERIFIED / REJECTED / COMPLETED)
```

**Attributes:** `api-base-url`, `application-id`, `session-token`

**Behaviour:**
- Fetches `GET /applications/{id}/documents/requirements` on mount
- Renders one `RequirementCard` per `DocumentRequirement`
- Each card handles its own upload via `POST /applications/{id}/documents/upload`
- Reloads requirements after each successful upload to reflect updated status
- Displays a progress bar (`N of M complete`) and a completion banner when all requirements are `COMPLETED`
- Rejected documents (virus scan failure) display an inline prompt to re-upload

The shell embeds `<document-upload-manager>` when `applicationStatus = DOCUMENTS_REQUIRED`, passing the applicant's session token as the `session-token` attribute.

---

# 10. Security Architecture Summary

| Concern | Mechanism |
|---------|-----------|
| Applicant authentication | `POST /applications/login` → JJWT session token (30-minute expiry) |
| Verification | `VerificationPort` (stub: SSN last-4 + DOB match; production: verification-service) |
| API authorization | JWT Bearer token on all post-login endpoints |
| PII protection | SSN stored as token, never logged in plaintext |
| Service-to-service | Internal REST (mTLS planned for production) |
| Secrets | Environment variables / secrets manager |

---

# 11. Observability

All services emit:

- Structured logs with `applicationId`, `correlationId`, service name, timestamp
- Spring Boot Actuator health and metrics endpoints
- Distributed trace headers (planned: OpenTelemetry)

---

# 12. Deployment Architecture

| Concern | Approach |
|---------|----------|
| Runtime | Spring Boot 3.3.6, Java 21 |
| Containerisation | Docker (one container per service) |
| Orchestration | Kubernetes |
| Messaging | Apache Kafka |
| Persistence | PostgreSQL (one schema per service) |
| Schema migration | Flyway |
| UI build | Vite + React + TypeScript + Tailwind CSS |

---

# 13. Related Documents

| Document | Location |
|----------|----------|
| Domain Boundaries and Context Map | `docs/architecture/000-domain-boundaries-and-context-map.md` |
| Application State Machine | `docs/architecture/002-application-state-machine.md` |
| Microservice Boundaries | `docs/architecture/002-microservice-boundaries.md` |
| Event-Driven Architecture | `docs/architecture/005-event-driven-architecture.md` |
| Security Architecture | `docs/architecture/007-security-architecture.md` |
| Functional Flow | `docs/flows/01-functional-flow.md` |
| Component Interaction Flows | `docs/flows/02-component-interaction-flows.md` |
| Scenario Flows | `docs/flows/03-scenario-flows.md` |
| OpenSpec — Post-Decision Flow | `openspec/changes/post-decision-flow/` |
