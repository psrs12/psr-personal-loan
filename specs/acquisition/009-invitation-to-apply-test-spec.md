# acquisition/invitation-to-apply/009-invitation-to-apply-test-spec.md

# Personal Loan Acquisition Platform

# Invitation To Apply Test Specification

Version: 1.0

Status: Draft

Owner: Acquisition Domain

---

# 1. Purpose

This document defines the test strategy and test cases for the Invitation To Apply (ITA) capability.

Objectives:

* Validate business correctness
* Validate integration behavior
* Ensure production readiness
* Prevent invalid application creation
* Verify audit and compliance requirements

---

# 2. Testing Scope

## In Scope

* Invitation validation
* Offer retrieval
* Applicant prefill
* Application creation
* State transitions
* API behavior
* Events
* Error handling

---

## Out Of Scope

* Offer generation testing
* Fraud decision testing
* Credit decision testing
* Funding testing

Those belong to respective systems.

---

# 3. Test Pyramid

```text
                 E2E Tests

              Integration Tests

             Contract Tests

              Unit Tests
```

---

# 4. Test Environment

Required:

```text
LOCAL

DEV

QA

UAT

PERFORMANCE
```

---

# 5. Unit Testing

## Objective

Validate business logic independently.

---

# 6. Domain Unit Tests

## Invitation Entity

Test:

* State transitions
* Business rules
* Validation rules

---

## Test Cases

| Test                | Expected Result |
| ------------------- | --------------- |
| Create invitation   | State CREATED   |
| Validate invitation | State VALIDATED |
| Expire invitation   | State EXPIRED   |
| Reject invitation   | State REJECTED  |

---

# 7. Invitation State Tests

## TC-ITA-001

Scenario:

Valid invitation validation

Given:

Invitation exists

When:

validate()

Then:

Status becomes VALIDATED

---

## TC-ITA-002

Scenario:

Invalid invitation

Given:

Invitation does not exist

When:

validate()

Then:

Status becomes INVALID

---

## TC-ITA-003

Scenario:

Expired offer

Given:

Offer expiration date passed

When:

retrieve offer

Then:

Status becomes EXPIRED

---

# 8. Application Service Tests

## TC-ITA-010

Scenario:

Start application successfully

Given:

Valid invitation

Active offer

When:

startApplication()

Expected:

Application created

Status:

STARTED

---

## TC-ITA-011

Duplicate application attempt

Given:

Invitation already used

Expected:

Error:

```text
ITA-006
```

---

# 9. API Testing

Framework:

* Spring MockMvc
* REST Assured

---

# 10. Validate Invitation API Tests

Endpoint:

```http
POST /invitations/validate
```

---

## TC-API-001

Valid invitation

Request:

```json
{
 "invitationId":"INV123"
}
```

Expected:

HTTP 200

---

## TC-API-002

Missing invitation ID

Expected:

HTTP 400

Error:

```text
ITA-005
```

---

## TC-API-003

Invitation not found

Expected:

HTTP 404

Error:

```text
ITA-001
```

---

# 11. Offer Retrieval API Tests

Endpoint:

```http
GET /invitations/{id}/offer
```

---

## TC-API-010

Active offer

Expected:

HTTP 200

---

## TC-API-011

Expired offer

Expected:

HTTP 422

Error:

ITA-002

---

## TC-API-012

Inactive offer

Expected:

HTTP 422

Error:

ITA-003

---

# 12. Application Start API Tests

Endpoint:

```http
POST /applications/start
```

---

## TC-API-020

Successful application creation

Expected:

HTTP 201

Response:

```json
{
 "status":"STARTED"
}
```

---

## TC-API-021

Duplicate request

Same:

Idempotency-Key

Expected:

Same application returned

---

# 13. Repository Integration Tests

Technology:

Testcontainers

---

Validate:

* Database mapping
* Queries
* Constraints

---

# 14. Database Tests

## TC-DB-001

Create invitation

Verify:

Record stored

---

## TC-DB-002

Find by invitation ID

Verify:

Correct record returned

---

## TC-DB-003

Duplicate invitation

Verify:

Unique constraint failure

---

# 15. Offer Management Integration Tests

Use:

WireMock

---

# 16. Offer API Happy Path

Mock response:

```json
{
 "offerId":"OFF123",
 "status":"ACTIVE"
}
```

Expected:

ITA continues workflow.

---

# 17. Offer API Failure Tests

## Timeout

Mock:

Timeout

Expected:

Retry triggered

---

## Service unavailable

Mock:

HTTP 503

Expected:

Circuit breaker behavior

---

## Unauthorized

Mock:

HTTP 401

Expected:

No retry

---

# 18. Contract Testing

Framework:

Consumer Driven Contract Testing

---

Producer:

Offer Management

Consumer:

ITA Service

---

Validate:

Request:

```text
Invitation ID
```

Response:

```text
Offer
Customer
Status
```

---

# 19. Event Testing

Framework:

Embedded Kafka

---

# 20. Event Publication Tests

## TC-EVENT-001

InvitationValidated

Given:

Successful validation

Verify:

Event published

Topic:

```text
acquisition.invitation.validated
```

---

# 21. Event Schema Tests

Validate:

Required fields:

```text
eventId

eventType

eventVersion

correlationId

payload
```

---

# 22. Event Ordering Tests

Verify:

Correct order:

```text
InvitationValidated

OfferRetrieved

ApplicationCreated
```

---

# 23. Duplicate Event Tests

Given:

Same eventId received twice

Expected:

Second event ignored

---

# 24. Security Testing

Validate:

Authentication

Authorization

Data protection

---

# 25. Authentication Tests

## TC-SEC-001

Missing token

Expected:

HTTP 401

---

## TC-SEC-002

Invalid token

Expected:

HTTP 401

---

# 26. Authorization Tests

User without role:

```text
application.create
```

Expected:

HTTP 403

---

# 27. Data Protection Tests

Verify:

Logs do not contain:

* SSN
* Address
* Tokens
* Secrets

---

# 28. Performance Testing

Tool:

JMeter / Gatling

---

# 29. Load Test

Scenario:

High invitation traffic

---

Target:

```text
1000 requests/minute
```

---

Validate:

* Response time
* Error rate
* CPU
* Memory

---

# 30. Performance Criteria

API:

P95:

```text
< 2 seconds
```

---

Error rate:

```text
< 1%
```

---

# 31. Resilience Testing

Test failures:

* Offer API unavailable
* Database unavailable
* Kafka unavailable

---

Expected:

Graceful handling

---

# 32. End To End Testing

Scenario:

Customer Journey

```text
Invitation

↓

Validation

↓

Offer Retrieval

↓

Prefill

↓

Application Created
```

---

# 33. UAT Scenarios

## UAT-001

Customer applies using invitation

Expected:

Application created.

---

## UAT-002

Customer uses expired invitation

Expected:

Friendly error.

---

## UAT-003

Customer retries same invitation

Expected:

Duplicate prevented.

---

# 34. Regression Tests

Executed every release.

Includes:

* Invitation flow
* API regression
* Integration regression
* Security regression

---

# 35. Quality Gates

Release requires:

☐ Unit tests pass

☐ Integration tests pass

☐ Contract tests pass

☐ Security tests pass

☐ Performance accepted

☐ No critical defects

---

# 36. Test Data

Use:

Synthetic test data only.

Never use:

Production customer data.

---

# 37. Definition Of Done

ITA is production ready when:

☐ All critical tests automated

☐ Coverage achieved

☐ Performance validated

☐ Security approved

☐ Events validated

☐ Monitoring verified

---

# Related Documents

* 001-invitation-to-apply-spec.md
* 002-state-machine.md
* 003-api-spec.md
* 004-data-model.md
* 005-integration-spec.md
* 005-testing-strategy.md
* 006-coding-standard.md
