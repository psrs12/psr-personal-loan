## 1. Service Bootstrap — compliance-orchestration-service

- [ ] 1.1 Create Spring Boot project `compliance-orchestration-service` (port 8086) with dependencies: Spring Web, Spring Data JPA, Spring Kafka, Flyway, Testcontainers
- [ ] 1.2 Define hexagonal package structure: `domain/`, `application/`, `infrastructure/`, `api/`
- [ ] 1.3 Add Flyway baseline migration: create `compliance_schema` placeholder
- [ ] 1.4 Configure Kafka consumer and producer beans; define topic names as constants
- [ ] 1.5 Add `GlobalExceptionHandler` with ProblemDetail format per platform error-handling standard

## 2. Application State Machine Extension — COMPLIANCE_HOLD

- [ ] 2.1 Add `COMPLIANCE_HOLD` state to `ApplicationStatus` enum in `application-management-service`
- [ ] 2.2 Define valid transitions: `PROCESSING → COMPLIANCE_HOLD`, `COMPLIANCE_HOLD → FUNDING_PENDING`, `COMPLIANCE_HOLD → DECLINED`
- [ ] 2.3 Add unit tests for `COMPLIANCE_HOLD` transitions in `ApplicationStateMachineTest`
- [ ] 2.4 Add `PATCH /applications/{id}/status` handling for `COMPLIANCE_HOLD` transition (internal endpoint)

## 3. Gate 1 — AML Pre-Screening

- [ ] 3.1 Define `AmlScreeningPort` interface in domain layer with `screen(AmlScreeningRequest): AmlScreeningResult`
- [ ] 3.2 Implement `FraudAmlPlatformAdapter` in infrastructure layer (WireMock stub for dev/test)
- [ ] 3.3 Implement `AmlPreScreeningUseCase`: consume `ApplicationSubmitted` Kafka event, call port, route on PASS/FAIL
- [ ] 3.4 Create `AmlScreeningRecord` JPA entity and Flyway migration
- [ ] 3.5 Implement idempotency check — skip duplicate `ApplicationSubmitted` events for already-screened applications
- [ ] 3.6 Publish `AmlScreeningPassed` on PASS; publish `AmlHoldPlaced` and call status API on FAIL
- [ ] 3.7 Implement circuit breaker on `FraudAmlPlatformAdapter` with configurable timeout
- [ ] 3.8 Unit tests: use case, port mock, PASS/FAIL/timeout scenarios
- [ ] 3.9 Integration test: full slice with Testcontainers PostgreSQL + Kafka; WireMock for AML Platform

## 4. Gate 2 — FCRA Credit Pull Consent Audit

- [ ] 4.1 Create `FcraConsentRecord` JPA entity and Flyway migration (`consent_id`, `application_id`, `applicant_id`, `selected_offer_id`, `consent_timestamp`, `recorded_at`, `credit_pull_type`)
- [ ] 4.2 Implement `RecordFcraConsentUseCase` with idempotency on `(application_id, credit_pull_type)`
- [ ] 4.3 Add `POST /compliance/fcra-consent` controller with request validation (`@Valid`)
- [ ] 4.4 Add `GET /compliance/fcra-consent/{applicationId}` controller
- [ ] 4.5 Publish `FcraConsentAuditConfirmed` event on successful record creation
- [ ] 4.6 Enforce immutability — no update/delete endpoints; add DB-level constraint
- [ ] 4.7 Extend `pricing-orchestration-service`: call `POST /compliance/fcra-consent` before initiating hard pull; only proceed on `200 OK`
- [ ] 4.8 Unit tests: use case with idempotency, missing field rejection, immutability
- [ ] 4.9 Integration test: full round-trip with pricing-orchestration-service (WireMock for compliance-orchestration-service)
- [ ] 4.10 Acceptance test: verify hard pull is NOT initiated if consent endpoint returns error

## 5. Gate 3 — Adverse Action

- [ ] 5.1 Create `AdverseActionRecord` JPA entity and Flyway migration
- [ ] 5.2 Implement `AdverseActionReasonMapper` with configurable reason code → ECOA description mapping (externalised config, no redeployment required)
- [ ] 5.3 Define `NotificationPort` interface with `sendAdverseActionNotice(AdverseActionNoticeRequest)`
- [ ] 5.4 Implement `NotificationPlatformAdapter` in infrastructure layer (WireMock stub for dev/test)
- [ ] 5.5 Implement `GenerateAdverseActionUseCase`: consume `FinalDecisionDeclined`, map reason, create record, call notification port
- [ ] 5.6 Implement retry logic for Notification Platform failures (max 3 retries, exponential backoff, dead-letter on exhaustion)
- [ ] 5.7 Publish `AdverseActionIssued` after successful notification submission
- [ ] 5.8 Implement scheduled timing monitor: find `DECLINED` applications with no `AdverseActionRecord` older than 24 hours; emit `AdverseActionOverdueAlert`
- [ ] 5.9 Add `GET /compliance/adverse-action/{applicationId}` retrieval endpoint
- [ ] 5.10 Unit tests: mapper, use case with unknown reason code, retry logic, timing monitor
- [ ] 5.11 Integration test: full slice with Testcontainers; WireMock for Notification Platform

## 6. Gate 4 — TILA Disclosure Audit

- [ ] 6.1 Extend `ESignCompleted` Kafka event schema: add `apr`, `totalOfPayments`, `financeCharge`, `loanTerm` fields
- [ ] 6.2 Extend `POST /applications/{id}/esign` request body in `offer-acceptance-service`: add TILA fields; return `422 TILA_FIELDS_REQUIRED` if missing
- [ ] 6.3 Read TILA values from `ConfirmedOffer` in `offer-acceptance-service` and populate `ESignCompleted` event
- [ ] 6.4 Create `TilaDisclosureAuditRecord` JPA entity and Flyway migration (immutable — no update columns)
- [ ] 6.5 Implement `CreateTilaDisclosureAuditUseCase`: consume `ESignCompleted`, validate TILA fields present, persist record, publish `TilaDisclosureAuditCreated`
- [ ] 6.6 Implement dead-letter routing for `ESignCompleted` events with missing TILA fields
- [ ] 6.7 Implement idempotency: skip duplicate `ESignCompleted` for already-audited applications
- [ ] 6.8 Add `GET /compliance/tila-disclosure/{applicationId}` retrieval endpoint
- [ ] 6.9 Enforce immutability at DB level (no UPDATE permitted on `tila_disclosure_audit_record`)
- [ ] 6.10 Unit tests: use case, missing field rejection, idempotency, immutability guard
- [ ] 6.11 Integration test: full slice with Testcontainers; verify record fields match event payload exactly
- [ ] 6.12 Acceptance test: verify `ESignCompleted` event contains TILA fields end-to-end

## 7. Gate 5 — Pre-Funding AML Re-Check

- [ ] 7.1 Implement `AmlPreFundingUseCase`: consume `FundingRequested` event, call `AmlScreeningPort` with `screeningType: PRE_FUNDING`
- [ ] 7.2 On PASS: publish `PreFundingCompliancePassed`; on FAIL: publish `PreFundingComplianceHeld`, call status API to set `COMPLIANCE_HOLD`
- [ ] 7.3 Add `POST /applications/{id}/compliance-hold/release` endpoint (authorised operator only)
- [ ] 7.4 Add `POST /applications/{id}/compliance-hold/escalate` endpoint (authorised operator only; triggers decline + adverse action)
- [ ] 7.5 Unit tests: use case, PASS/FAIL/timeout scenarios, hold release, escalation to decline
- [ ] 7.6 Integration test: full slice with Testcontainers; WireMock for AML Platform

## 8. Observability

- [ ] 8.1 Add structured logs at every gate entry and exit with `applicationId`, `gateType`, `outcome`, `correlationId`
- [ ] 8.2 Add metrics counters: gate pass rate, gate fail rate, hold rate — per gate type
- [ ] 8.3 Ensure no PII (SSN, full name, DOB) appears in any log output from compliance-orchestration-service
- [ ] 8.4 Add health check endpoint; register compliance-orchestration-service in service registry

## 9. Capability Spec

- [ ] 9.1 Create `openspec/compliance-orchestration/spec.md` as the standalone capability spec (mirroring structure of existing capability specs)
- [ ] 9.2 Update `openspec/offer-acceptance/spec.md` to reflect TILA field extension to e-sign flow
- [ ] 9.3 Update `openspec/pricing-orchestration/spec.md` to reflect FCRA gate dependency before hard pull
- [ ] 9.4 Update `openspec/application-management/spec.md` to include `COMPLIANCE_HOLD` state and transitions
