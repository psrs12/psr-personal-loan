# Personal Loan Acquisition Platform

Enterprise-grade Personal Loan Acquisition Platform built on Domain-Driven Design, Hexagonal Architecture, and Event-Driven Architecture.

---

## Platform Overview

The platform manages the complete customer journey from invitation through to loan funding. It orchestrates enterprise capabilities (credit, fraud, identity, decisioning, funding) while owning the acquisition workflow end-to-end.

---

## Architecture

| Concern | Approach |
|---------|----------|
| Domain Design | Domain-Driven Design — bounded contexts, aggregates, value objects |
| Service Design | Hexagonal Architecture (ports and adapters) |
| Integration | Event-Driven Architecture — Apache Kafka choreography |
| API | REST, API-First, versioned contracts |
| UI | Micro-frontend shell (Vite + React + TypeScript + Tailwind CSS) |
| Backend | Java 21, Spring Boot 3.3.6 |
| Persistence | PostgreSQL (one schema per service), Flyway migrations |

---

## Services

### Backend

| Service | Port | Responsibility |
|---------|------|----------------|
| `invitation-service` | 8080 | Invitation token generation and validation |
| `application-management-service` | 8081 | Application lifecycle, state machine, login, event timeline |
| `pricing-orchestration-service` | 8082 | Soft pull, offer pricing, hard pull, final decision routing |
| `offer-acceptance-service` | 8085 | Declarations, e-signature capture, ESignCompleted event |
| `document-service` | 8084 | Document requirements, upload, virus scan, completion tracking |

### Frontend

| UI | Responsibility |
|----|----------------|
| `application-management-ui` | Micro-frontend shell — ITA flow web components + applicant self-service portal (login, state routing, post-decision MFE host) |
| `pricing-offers-ui` | Standalone `<pricing-offer-selector>` web component — offer list, offer selection, hard pull consent |
| `document-management-ui` | Standalone `<document-upload-manager>` web component — document requirements display, file upload, upload progress and status |

---

## Application Lifecycle

```
CREATED → STARTED → IN_PROGRESS → SUBMITTED → PROCESSING
                                                   │
                          ┌────────────────────────┤
                          │                        │
                       DECLINED               APPROVED
                      (terminal)                   │
                                      ┌────────────┤
                                      │            │
                             DOCUMENTS_REQUIRED  (direct e-sign)
                                      │            │
                                 UNDERWRITING       │
                                      │            │
                                   APPROVED ◄───────┘
                                      │
                                OFFER_ACCEPTED
                                      │
                               FUNDING_PENDING → FUNDED → COMPLETED
```

---

## Documentation

### Architecture
| Document | Description |
|----------|-------------|
| [Architecture Overview](docs/architecture/000-architecture-overview.md) | System context, service landscape, event topology, security summary |
| [Domain Boundaries and Context Map](docs/architecture/000-domain-boundaries-and-context-map.md) | Bounded contexts, ownership boundaries, enterprise dependencies |
| [Application State Machine](docs/architecture/002-application-state-machine.md) | All states, transitions, ownership rules, self-service routing |
| [Microservice Boundaries](docs/architecture/002-microservice-boundaries.md) | Service decomposition strategy and data ownership |
| [Event-Driven Architecture](docs/architecture/005-event-driven-architecture.md) | Kafka patterns, event ownership, consumer rules |
| [Security Architecture](docs/architecture/007-security-architecture.md) | Authentication, authorisation, PII handling |
| [Deployment Architecture](docs/architecture/009-deployment-architecture.md) | Runtime, containerisation, environments |

### Flow Documents
| Document | Description |
|----------|-------------|
| [Functional Flow](docs/flows/01-functional-flow.md) | End-to-end business journey narrative — all phases from invitation to funding |
| [Component Interaction Flows](docs/flows/02-component-interaction-flows.md) | Sequence diagrams for all major platform interactions |
| [Scenario Flows](docs/flows/03-scenario-flows.md) | Step-by-step walkthroughs: approved, documents required, declined, login, virus rejection, idempotency |

### Design
| Document | Description |
|----------|-------------|
| [Service Design Guidelines](docs/design/001-service-design-guidelines.md) | Hexagonal architecture patterns per service |
| [Database Design Guidelines](docs/design/002-database-design-guidelines.md) | Schema ownership, migration strategy |
| [Testing Strategy](docs/design/005-testing-design.md) | Unit, integration, contract testing approach |

### Standards
| Document | Description |
|----------|-------------|
| [Error Handling Standard](docs/standards/003-error-handling-standard.md) | Problem Detail format, error codes |
| [Logging Standard](docs/standards/004-logging-standard.md) | Structured logging, required fields |
| [Coding Standard](docs/standards/006-coding-standard.md) | Code conventions, layer rules |

### Capability Specifications (OpenSpec)
| Capability | Location |
|------------|----------|
| Application Management | `openspec/application-management/` |
| Post-Decision Flow (active change) | `openspec/changes/post-decision-flow/` |

---

## Technology Stack

**Backend**
- Java 21
- Spring Boot 3.3.6
- Spring Data JPA
- Spring Kafka
- PostgreSQL
- Flyway
- JJWT 0.12.6

**Frontend**
- TypeScript
- React
- Vite
- Tailwind CSS
- Web Components

**Testing**
- JUnit 5
- Mockito
- Testcontainers
- WireMock

**Infrastructure**
- Apache Kafka
- Docker / Kubernetes
- PostgreSQL (per service)
