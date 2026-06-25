# design/003-error-handling-standard.md

# Personal Loan Acquisition Platform

## Error Handling Standard

Version: 1.0

Status: Draft

Owner: Application Architecture

---

# 1. Purpose

This document defines error handling standards for all services within the Personal Loan Acquisition Platform.

Objectives:

* Consistent error handling
* Consistent API responses
* Improved troubleshooting
* Improved customer experience
* Better observability
* Better supportability

---

# 2. Error Handling Principles

## ERR-001 Fail Fast

Invalid requests should fail immediately.

---

## ERR-002 Consistent Error Contract

All APIs must return the same error structure.

---

## ERR-003 Business vs Technical Separation

Business errors and technical errors must be handled differently.

---

## ERR-004 No Internal Leakage

Internal implementation details must never be exposed.

Never expose:

* Stack traces
* SQL errors
* Database names
* Infrastructure details

---

## ERR-005 Traceability

Every error must contain:

* Correlation ID
* Timestamp
* Error Code

---

# 3. Error Categories

The platform uses four primary categories.

---

## Business Errors

Expected business rule violations.

Examples:

* Invalid application state
* Offer expired
* Invitation already used
* Application already submitted

---

## Validation Errors

Input validation failures.

Examples:

* Missing field
* Invalid email
* Invalid SSN format

---

## Integration Errors

External dependency failures.

Examples:

* Fraud unavailable
* Credit unavailable
* Offer Management unavailable

---

## System Errors

Unexpected failures.

Examples:

* Database outage
* Memory issue
* Infrastructure failure

---

# 4. Error Classification Matrix

| Category            | Retry | HTTP |
| ------------------- | ----- | ---- |
| Validation          | No    | 400  |
| Authentication      | No    | 401  |
| Authorization       | No    | 403  |
| Not Found           | No    | 404  |
| Business Rule       | No    | 422  |
| Conflict            | No    | 409  |
| Integration Failure | Yes   | 503  |
| System Failure      | Yes   | 500  |

---

# 5. Standard Error Response

All APIs must return:

```json
{
  "error": {
    "code": "APP-001",
    "message": "Application not found",
    "category": "BUSINESS",
    "severity": "MEDIUM"
  },
  "correlationId": "abc123",
  "timestamp": "2026-06-24T12:00:00Z"
}
```

---

# 6. Error Response Fields

## code

Unique application error code.

Example:

```text
APP-001
```

---

## message

Customer-friendly message.

---

## category

Examples:

```text
VALIDATION

BUSINESS

INTEGRATION

SYSTEM
```

---

## severity

Examples:

```text
LOW

MEDIUM

HIGH

CRITICAL
```

---

## correlationId

Used for troubleshooting.

---

# 7. Error Code Standard

Format:

```text
<SERVICE>-<NUMBER>
```

Examples:

```text
APP-001

APP-002

ITA-001

ITA-002

DOC-001

CRD-001

FRD-001
```

---

# 8. Service Prefixes

| Service          | Prefix |
| ---------------- | ------ |
| Application      | APP    |
| Invitation       | ITA    |
| Offer Acceptance | OFF    |
| Document         | DOC    |
| Underwriting     | UWR    |
| Identity         | IDV    |
| Fraud            | FRD    |
| Credit           | CRD    |
| Decision         | DEC    |
| Funding          | FND    |
| Notification     | NTF    |

---

# 9. Validation Errors

Example:

```json
{
  "error": {
    "code": "APP-VAL-001",
    "message": "Email address is required",
    "category": "VALIDATION"
  }
}
```

---

# 10. Business Exceptions

Examples:

```text
InvitationExpiredException

InvitationAlreadyUsedException

ApplicationAlreadySubmittedException

OfferExpiredException
```

---

# 11. Technical Exceptions

Examples:

```text
DatabaseUnavailableException

EventPublishingException

IntegrationTimeoutException
```

---

# 12. Exception Hierarchy

Recommended:

```text
LoanApplicationException

|

+-- ValidationException

|

+-- BusinessException

|

+-- IntegrationException

|

+-- TechnicalException
```

---

# 13. Spring Boot Exception Model

Base exception:

```java
public abstract class LoanApplicationException
       extends RuntimeException {

   private final String errorCode;

}
```

---

Business exception:

```java
public class InvitationExpiredException
       extends BusinessException {
}
```

---

# 14. Global Exception Handler

Every service must implement:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
}
```

---

Responsibilities:

* Exception mapping
* Error transformation
* Correlation ID inclusion

---

# 15. Validation Error Handling

Use:

```java
@Valid
```

Example:

```java
@PostMapping
public Response create(
    @Valid Request request) {
}
```

---

Validation failures automatically mapped to:

HTTP 400

---

# 16. Integration Error Handling

Integration failures should be wrapped.

Never expose:

```text
SocketTimeoutException
```

Instead:

```text
CreditServiceUnavailableException
```

---

# 17. Timeout Handling

Timeouts are integration errors.

Example:

```text
Offer Management Timeout
```

Mapped to:

```text
HTTP 503
```

---

# 18. Retryable Errors

Retry allowed:

* Network failure
* Timeout
* HTTP 503
* HTTP 429

---

Retry not allowed:

* Validation errors
* Business errors
* Authentication errors

---

# 19. Circuit Breaker Integration

When circuit breaker opens:

Return:

```json
{
 "error":{
   "code":"CRD-503",
   "message":"Credit service unavailable"
 }
}
```

---

# 20. Logging Errors

Every error log must include:

```text
correlationId

applicationId

errorCode

serviceName
```

---

Example:

```json
{
 "level":"ERROR",
 "service":"credit-service",
 "errorCode":"CRD-503",
 "correlationId":"abc123"
}
```

---

# 21. Security Errors

Authentication failure:

```text
401
```

Authorization failure:

```text
403
```

Never disclose:

* Permission details
* Security implementation

---

# 22. Event Processing Errors

Consumer failure:

```text
Retry

↓

Retry

↓

Retry

↓

DLQ
```

---

Failed events must be auditable.

---

# 23. Batch Error Handling

If future batch processing exists:

Capture:

* Record count
* Failed records
* Failure reason

---

Continue processing where appropriate.

---

# 24. Customer-Friendly Messages

Good:

```text
Unable to process application at this time.
```

Bad:

```text
ORA-00001 Unique Constraint Violation
```

---

# 25. Error Monitoring

Track:

* Error count
* Error rate
* Top errors
* Retry count
* Integration failures

---

# 26. Alerting Thresholds

Critical:

* Application submission unavailable
* Database unavailable
* Decision platform unavailable

High:

* Increased validation failures
* Increased integration failures

---

# 27. Audit Requirements

Capture:

* Error code
* Timestamp
* Service
* Correlation ID
* User ID (if available)

---

# 28. Production Support Guidelines

Support teams should troubleshoot using:

```text
Correlation ID

↓

Trace ID

↓

Logs

↓

Events

↓

Root Cause
```

---

# 29. Error Handling Checklist

Before production:

☐ Error codes defined

☐ Exception hierarchy implemented

☐ Global handler implemented

☐ Integration errors mapped

☐ Logging verified

☐ Monitoring configured

☐ Alerts configured

---

# Related Documents

001-service-design-guidelines.md

002-database-design-guidelines.md

004-logging-standard.md

005-testing-strategy.md

006-coding-standard.md

004-api-standards.md

008-observability-architecture.md
