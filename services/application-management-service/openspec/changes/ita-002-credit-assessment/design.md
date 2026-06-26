## Decisions

### D-001: CreditAssessmentRequested carries bureau policy, not Credit Management

Personal Loan owns which bureaus to call and the freshness window. These are product-level policy decisions. Credit Management is infrastructure — it executes the request but does not determine its scope.

```
CreditAssessmentRequested {
  applicationId,
  customerReferenceId,
  bureaus: [EXPERIAN, TRANSUNION, EQUIFAX],   ← PL decides
  freshnessWindowDays: 30                      ← PL decides
}
```

Bureau policy (which bureaus, freshness threshold) is configurable per product in a `bureau_pull_policy` table — changeable by ops without deployment.

### D-002: Primary bureau as mandatory processing gate

Personal Loan configures one bureau as primary per product. If the primary bureau's `CreditReportRetrieved` event does not arrive within the timeout window, the application transitions to `CREDIT_ASSESSMENT_FAILED`. No decision engine call is made.

Secondary and tertiary bureaus are best-effort. Missing secondary data triggers compensating rules in the decision engine — not a hard stop.

### D-003: Fan-in tracking with configurable timeout window

A `credit_bureau_response` table tracks which bureaus have responded per application. A scheduled processor (or event-driven timer) evaluates completion:

```
All bureaus received → proceed immediately
Primary received + timeout elapsed → proceed with available data
Primary not received + timeout elapsed → CREDIT_ASSESSMENT_FAILED
```

Timeout is configurable per product (`bureau_pull_policy.timeout_minutes`).

### D-004: Credit Assessment Context assembled by Personal Loan Processor

Before calling the decision engine, Personal Loan Processor assembles a `CreditAssessmentContext`:

```
{
  applicationId,
  applicantId,
  bureauStatus: [
    { bureau, reportId, isPrimary, dataSource: LIVE|CACHED, pulledAt, received }
  ]
}
```

This context includes freshness metadata (`dataSource`, `pulledAt`) so the decision engine can apply confidence-weighted rules without needing to know how Credit Management manages its cache.

### D-005: Decision Engine call is synchronous with 5-second SLA

The Personal Loan Processor (an event consumer, not a user-facing thread) calls the Decision Engine synchronously. A 5-second SLA is contractually agreed. Timeout is set at 8 seconds. On timeout or DE failure, the application transitions to `REFERRED_FOR_REVIEW` for manual handling.

Resilience4j circuit breaker protects the DE call.

### D-006: Decision Engine fetches bureau data lazily from Credit Management

The Decision Engine receives `reportId` references, not raw bureau data. It fetches only what its active rules require from the Credit Management API. This keeps the event payload small and raw bureau data inside the Credit Management boundary (PII containment).

### D-007: Customer-visible states are separate from internal processing states

Internal state drives system processing. Customer-visible state is derived from internal state via a mapping:

```
Internal states → Customer-visible state
──────────────────────────────────────────────────────
CREATED                          → Application Received
CREDIT_ASSESSMENT_INITIATED      → Under Review
CREDIT_ASSESSMENT_IN_PROGRESS    → Under Review
CREDIT_ASSESSMENT_COMPLETE       → Under Review
DECISION_ENGINE_PROCESSING       → Under Review
APPROVED                         → Decision Ready
DECLINED                         → Decision Ready
REFERRED_FOR_REVIEW              → Additional Info Needed
CREDIT_ASSESSMENT_FAILED         → Under Review (ops alerted)
CANCELLED / EXPIRED              → Application Cancelled
```

The customer-facing API returns the customer-visible label, never the internal state name.

### D-008: Three new domain events

```
Published by this service:
  CreditAssessmentRequested  → Credit Management subscribes
  DecisionReached            → downstream (notification, disbursement)

Consumed by this service:
  CreditReportRetrieved      → Credit Management publishes
```
