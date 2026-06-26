## Context

This is the initial implementation of the Application Management service. The service handles the Personal Loan application lifecycle starting with Invitation To Apply (ITA) intake. The platform follows Hexagonal Architecture, Domain Driven Design, and Event Driven Architecture. All external system interactions are via adapters; domain logic is isolated from infrastructure concerns.

Stack: Java 21, Spring Boot 3, PostgreSQL, Flyway, Kafka, JUnit 5, Mockito, Testcontainers.

## Goals / Non-Goals

**Goals:**
- Implement the ITA intake flow: validate invitation → retrieve offer → retrieve customer prefill → create InvitationSession + ApplicationIntakeContext → return prefill response
- Implement application creation: accept intakeId + applicant data → create Application + Applicant + LoanRequest + ApplicationOffer → publish event
- Enforce all business rules: invitation validity, offer availability, session expiry, duplicate prevention, prefill field restrictions
- Build with hexagonal architecture: clean separation between domain, application, infrastructure, and API layers

**Non-Goals:**
- Application update, resume, or submission flows (future changes)
- Direct intake channel implementation (future change)
- Credit decisioning or downstream processing
- Customer profile maintenance

## Decisions

### D-001: Hexagonal Architecture with ports and adapters

The domain layer defines ports (interfaces) for external systems. Infrastructure implements adapters. The domain has zero dependency on Spring, JPA, or HTTP.

- `OfferManagementPort` → `OfferManagementAdapter` (REST client to Offer Management Platform)
- `CustomerProfilePort` → `CustomerProfileAdapter` (REST client to Customer Profile Platform)
- `InvitationSessionRepository` → `InvitationSessionJpaAdapter`
- `ApplicationIntakeContextRepository` → `ApplicationIntakeContextJpaAdapter`
- `ApplicationRepository` → `ApplicationJpaAdapter`
- `ApplicationEventPublisher` → `KafkaApplicationEventPublisher`

**Alternative considered**: Anemic domain model with all logic in services. Rejected — violates DDD and makes business rules hard to test in isolation.

### D-002: Invitation processing as a single application use case

`ProcessInvitationUseCase` orchestrates the full intake flow: validate → retrieve offer → retrieve customer → create session → create intake context → return prefill. This is a single transactional boundary for intake creation. Customer lookup failure does not roll back the transaction — the intake context is created with `prefillStatus = PARTIAL`.

**Alternative considered**: Separate use cases for each step with saga pattern. Rejected — over-engineering for a synchronous flow; adds complexity without benefit at this stage.

### D-003: Application creation as a separate use case

`CreateApplicationUseCase` is independent of invitation processing. It accepts an `intakeId`, validates the session has not expired, loads the intake context, creates the Application aggregate, snapshots offer terms into ApplicationOffer, and publishes the event.

**Rationale**: The user stated explicitly that invitation processing produces prefill data only; the applicant completes the form before the application is created. This matches the API design (`POST /invitations/initialize` and `POST /applications` are separate calls).

### D-004: Offer snapshot at application creation time

Offer terms are snapshotted twice:
1. At intake time → `offer_snapshot` table (12-month retention, linked to InvitationSession)
2. At application creation time → `application_offer` table (7-year retention, linked to Application)

The application-time snapshot is independent and survives session cleanup. It stores `customerReferenceId` from the offer but does NOT store customer PII (name, address, phone, email).

### D-005: Customer PII not persisted at intake time

Customer details retrieved from the Customer Profile Platform (name, address) are returned to the caller as prefill data only. They are not persisted by the intake service. The applicant's confirmed/entered details are persisted in the `applicant` table at application creation time.

**Rationale**: Minimises PII duplication. The applicant table is the single authoritative store for applicant data.

### D-006: Session expiry enforced at application creation

InvitationSession has a 30-minute TTL. Expiry is stored as `expiration_timestamp` in the database and checked at `CreateApplicationUseCase` execution time. Expired sessions return `INTAKE_EXPIRED`. The prospect must re-initialize the invitation to get a new session.

### D-007: Resilience via circuit breaker on external adapters

Both `OfferManagementAdapter` and `CustomerProfileAdapter` use Resilience4j circuit breaker and retry.

- Offer Management: failure → `OFFER_UNAVAILABLE` (503), intake aborted
- Customer Profile: failure → prefill partial, intake continues

**Rationale**: Offer data is mandatory for ITA intake; customer data is best-effort prefill only.

## Risks / Trade-offs

- **Synchronous external calls** → If Offer Management Platform is slow, intake latency increases. Mitigation: configure aggressive timeouts (2s), circuit breaker to fail fast.
- **30-minute session window** → If the prospect takes longer than 30 minutes on the form, they must restart. Mitigation: this is configurable via application property; can be extended if business requires.
- **Offer snapshot duplication** → Offer terms stored twice (offer_snapshot + application_offer). Mitigation: intentional design for different retention requirements; storage cost is negligible.

## Migration Plan

1. Create Flyway migrations for all tables: `invitation_session`, `offer_snapshot`, `application_intake_context`, `application`, `applicant`, `loan_request`, `application_offer`, `application_audit`.
2. Deploy service with feature disabled.
3. Enable API endpoints.
4. No rollback data migration required — new schema only.

## Open Questions

- What is the configured timeout for Offer Management Platform and Customer Profile Platform calls? (Assume 2s default, confirm with integration team.)
- What Kafka topic name should `ApplicationCreated` events be published to? (Assume `application-events`, confirm with platform team.)
