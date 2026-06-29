## Why

Once an application is created, the platform needs to assess creditworthiness before an underwriting decision can be made. This requires orchestrating credit bureau pulls through an enterprise Credit Management service, coordinating responses across up to three bureaus, and feeding the results into a Personal Loan decision engine that applies configurable rules — including deviation detection (SSN/name/DOB mismatches) and compensating rules when bureau data is missing or stale.

## What Changes

- Personal Loan publishes `CreditAssessmentRequested` event after `ApplicationCreated`
- Personal Loan configures which bureaus to request and the freshness policy (per product)
- Personal Loan tracks bureau responses in a new `credit_bureau_response` table
- Primary bureau is a mandatory gate — processing cannot continue without it
- A configurable timeout window closes the fan-in — processing proceeds with whatever secondary bureau data arrived
- Personal Loan Processor assembles a `CreditAssessmentContext` and calls the Decision Engine synchronously (5-second SLA)
- Decision Engine fetches bureau report data lazily from Credit Management API using `reportId` references
- Decision Engine applies rules: deviation check, missing bureau compensation, underwriting
- Application state machine extended with credit assessment and decision processing states
- Customer-visible states separated from internal processing states
- `DecisionReached` event published on completion

## Capabilities

### New Capabilities
- `credit-assessment-orchestration`: Publish bureau pull requests, track fan-in responses, enforce primary bureau gate, manage timeout window, assemble context for decision engine
- `decision-engine-integration`: Synchronous integration with Personal Loan decision engine — context assembly, report reference passing, freshness metadata, result handling
- `application-state-machine`: Extended state machine covering credit assessment phases, decision engine processing, and customer-visible state mapping

### Modified Capabilities
- `application-creation`: Application creation now triggers credit assessment — `ApplicationCreated` event initiates the next processing step

## Impact

- New Kafka event: `CreditAssessmentRequested` (published by this service)
- New Kafka event: `CreditReportRetrieved` (consumed from Credit Management)
- New Kafka event: `DecisionReached` (published by this service)
- New tables: `credit_bureau_response`, `bureau_pull_policy`, `application_decision`
- New integration: Credit Management API (read bureau reports by reportId)
- New integration: Personal Loan Decision Engine API (synchronous, 5s SLA)
- Application state machine gains ~6 new internal states
- `ApplicationStatus` enum extended
- No changes to existing intake or SSN verification API contracts
