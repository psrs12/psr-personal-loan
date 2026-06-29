## 1. Domain Layer — Events and Value Objects

- [ ] 1.1 Create `CreditAssessmentRequested` domain event (applicationId, customerReferenceId, bureaus, freshnessWindowDays)
- [ ] 1.2 Create `CreditReportRetrieved` domain event (applicationId, customerReferenceId, bureauName, reportId, dataSource, pulledAt)
- [ ] 1.3 Create `DecisionReached` domain event (applicationId, decision, timestamp)
- [ ] 1.4 Create `Bureau` enum (EXPERIAN, TRANSUNION, EQUIFAX)
- [ ] 1.5 Create `DataSource` enum (LIVE, CACHED)
- [ ] 1.6 Create `DecisionOutcome` enum (APPROVED, DECLINED, REFERRED_FOR_REVIEW)
- [ ] 1.7 Create `CreditAssessmentContext` value object (applicationId, applicantId, bureauStatus list)
- [ ] 1.8 Create `BureauStatus` value object (bureau, reportId, isPrimary, dataSource, pulledAt, received)
- [ ] 1.9 Create `DecisionEngineResult` value object (applicationId, decision, rulesApplied, confidence)

## 2. Domain Layer — Aggregates and Ports

- [ ] 2.1 Extend `ApplicationStatus` enum with: CREDIT_ASSESSMENT_INITIATED, CREDIT_ASSESSMENT_IN_PROGRESS, CREDIT_ASSESSMENT_COMPLETE, DECISION_ENGINE_PROCESSING, CREDIT_ASSESSMENT_FAILED
- [ ] 2.2 Add status transition methods to `Application` aggregate: `inititateCreditAssessment()`, `creditReportReceived()`, `completeCreditAssessment()`, `failCreditAssessment()`
- [ ] 2.3 Create `CreditBureauResponse` entity (applicationId, bureau, reportId, isPrimary, dataSource, pulledAt, receivedAt)
- [ ] 2.4 Create `ApplicationDecision` entity (applicationId, decision, decidedAt, confidence)
- [ ] 2.5 Create `BureauPullPolicy` value object (primaryBureau, bureaus, freshnessWindowDays, timeoutMinutes)
- [ ] 2.6 Create `CreditBureauResponseRepository` port (save, findByApplicationId, hasReceivedPrimary)
- [ ] 2.7 Create `ApplicationDecisionRepository` port (save, findByApplicationId)
- [ ] 2.8 Create `BureauPullPolicyRepository` port (findForProduct)
- [ ] 2.9 Create `DecisionEnginePort` port (evaluate(context): DecisionEngineResult)
- [ ] 2.10 Create `CreditManagementPort` port (fetchReport(reportId): CreditReport)

## 3. Database Migrations

- [ ] 3.1 Flyway V10: `credit_bureau_response` table (id, application_id, bureau_name, report_id, is_primary, data_source, pulled_at, received_at)
- [ ] 3.2 Flyway V11: `bureau_pull_policy` table (id, product_code, primary_bureau, bureaus, freshness_window_days, timeout_minutes)
- [ ] 3.3 Flyway V12: `application_decision` table (id, application_id, decision, decided_at, confidence, rules_applied JSONB)
- [ ] 3.4 Flyway V13: seed `bureau_pull_policy` with Personal Loan defaults (primary=EXPERIAN, all 3 bureaus, 30-day freshness, 5-minute timeout)

## 4. Application Layer — Use Cases

- [ ] 4.1 Create `InitiateCreditAssessmentUseCase`: load bureau policy, publish `CreditAssessmentRequested`, transition application to CREDIT_ASSESSMENT_INITIATED
- [ ] 4.2 Create `RecordCreditReportUseCase`: consume `CreditReportRetrieved`, save to `credit_bureau_response`, transition to CREDIT_ASSESSMENT_IN_PROGRESS, check if all bureaus received (trigger immediate evaluation if so)
- [ ] 4.3 Create `EvaluateCreditAssessmentUseCase`: check primary bureau gate, assemble `CreditAssessmentContext`, call `DecisionEnginePort`, save `ApplicationDecision`, transition application state, publish `DecisionReached`
- [ ] 4.4 Create `CreditAssessmentTimeoutProcessor`: scheduled task — find applications in CREDIT_ASSESSMENT_IN_PROGRESS beyond timeout, trigger `EvaluateCreditAssessmentUseCase` or fail if primary missing

## 5. Infrastructure — JPA Adapters

- [ ] 5.1 Create `CreditBureauResponseJpaEntity` and `CreditBureauResponseJpaRepository`
- [ ] 5.2 Create `BureauPullPolicyJpaEntity` and `BureauPullPolicyJpaRepository`
- [ ] 5.3 Create `ApplicationDecisionJpaEntity` and `ApplicationDecisionJpaRepository`
- [ ] 5.4 Create `CreditAssessmentJpaAdapter` implementing `CreditBureauResponseRepository`, `ApplicationDecisionRepository`, `BureauPullPolicyRepository`

## 6. Infrastructure — External Adapters

- [ ] 6.1 Create `DecisionEngineAdapter` implementing `DecisionEnginePort` with Resilience4j circuit breaker (8-second timeout, no retry)
- [ ] 6.2 Create `CreditManagementAdapter` implementing `CreditManagementPort` with Resilience4j circuit breaker and retry
- [ ] 6.3 Create decision engine request/response DTOs
- [ ] 6.4 Create credit management API DTOs
- [ ] 6.5 Add `decisionEngineRestClient` and `creditManagementRestClient` beans to `RestClientConfig`
- [ ] 6.6 Add integration config for Decision Engine and Credit Management to `application.yml`

## 7. Infrastructure — Kafka

- [ ] 7.1 Create `CreditReportRetrievedListener` — Kafka consumer for `CreditReportRetrieved` event, delegates to `RecordCreditReportUseCase`
- [ ] 7.2 Update `KafkaApplicationEventPublisher` to publish `CreditAssessmentRequested` and `DecisionReached` events
- [ ] 7.3 Configure new Kafka topics in `application.yml`: `credit-assessment-requests`, `credit-report-received`, `decision-events`

## 8. Application State Machine

- [ ] 8.1 Update `Application` aggregate with new status transitions and guard methods
- [ ] 8.2 Add customer-visible state mapping (internal → customer label)
- [ ] 8.3 Expose customer-visible state on `GET /applications/{id}/status` API endpoint

## 9. Tests

- [ ] 9.1 Unit test `InitiateCreditAssessmentUseCase` — policy loaded, event published, correct bureaus
- [ ] 9.2 Unit test `RecordCreditReportUseCase` — primary received, secondary received, all received triggers evaluation
- [ ] 9.3 Unit test `EvaluateCreditAssessmentUseCase` — primary gate, context assembly, DE call, all decision outcomes
- [ ] 9.4 Unit test `CreditAssessmentTimeoutProcessor` — timeout triggers evaluation, primary missing → failed
- [ ] 9.5 Integration test `DecisionEngineAdapter` with WireMock — success, timeout, circuit breaker
- [ ] 9.6 Integration test `CreditManagementAdapter` with WireMock — report fetched, not found
- [ ] 9.7 Integration test `CreditReportRetrievedListener` with Testcontainers Kafka
- [ ] 9.8 Acceptance test: full ITA flow — application created → bureaus received → DE decision → DecisionReached event
- [ ] 9.9 Acceptance test: primary bureau missing → CREDIT_ASSESSMENT_FAILED
- [ ] 9.10 Acceptance test: secondary bureau missing → DE runs with compensating rules
- [ ] 9.11 Acceptance test: DE timeout → REFERRED_FOR_REVIEW
