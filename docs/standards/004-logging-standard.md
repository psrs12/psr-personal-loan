# design/004-logging-standard.md

# Personal Loan Acquisition Platform

## Logging Standard

Version: 1.0

Status: Draft

Owner: Application Architecture

---

# 1. Purpose

This document defines logging standards for all services within the Personal Loan Acquisition Platform.

Objectives:

* Consistent logging
* End-to-end traceability
* Production troubleshooting
* Audit support
* Security compliance
* Operational observability

---

# 2. Logging Principles

## LOG-001 Structured Logging

All logs must be machine-readable.

Preferred format:

JSON

---

## LOG-002 Correlation Everywhere

Every request, event, and integration call must include correlation identifiers.

---

## LOG-003 No Sensitive Data

PII and secrets must never appear in logs.

---

## LOG-004 Business Traceability

Business activities must be traceable end-to-end.

---

## LOG-005 Operational Value

Every log entry should provide operational value.

Avoid unnecessary logging.

---

# 3. Logging Architecture

```text
Customer Request

      |

API Gateway

      |

Application Service

      |

Fraud Service

      |

Credit Service

      |

Decision Service


      |

Central Logging Platform
```

---

# 4. Log Categories

The platform uses four log categories.

---

## Application Logs

Purpose:

Application execution details.

Examples:

* Application created
* Invitation validated
* Offer accepted

---

## Integration Logs

Purpose:

External system communication.

Examples:

* Fraud request
* Credit response
* Decision timeout

---

## Security Logs

Purpose:

Authentication and authorization activities.

Examples:

* Login success
* Login failure
* Access denied

---

## Audit Logs

Purpose:

Compliance and business audits.

Examples:

* Status change
* Consent captured
* Offer acceptance

---

# 5. Required Log Fields

Every log entry must contain:

```text
timestamp

serviceName

environment

logLevel

correlationId

traceId

message
```

---

Recommended:

```text
applicationId

userId

eventType

requestId
```

---

# 6. Example Log Entry

```json
{
  "timestamp":"2026-06-24T10:00:00Z",
  "serviceName":"application-service",
  "environment":"prod",
  "logLevel":"INFO",
  "correlationId":"abc123",
  "traceId":"xyz789",
  "applicationId":"APP10001",
  "message":"Application submitted"
}
```

---

# 7. Correlation Standards

Every request must have:

```text
X-Correlation-ID
```

---

Every service must:

* Generate if missing
* Propagate downstream
* Include in logs

---

Example:

```text
Customer

↓

Gateway

↓

Application Service

↓

Fraud Service

↓

Credit Service

↓

Decision Service
```

All share:

```text
correlationId=abc123
```

---

# 8. Trace Standards

Distributed tracing requires:

```text
traceId

spanId
```

---

Example:

```text
Trace

|

+-- Application

|

+-- Fraud

|

+-- Credit

|

+-- Decision
```

---

# 9. Log Levels

---

## ERROR

Unexpected failure.

Examples:

* Database unavailable
* Integration failure
* Application crash

---

## WARN

Recoverable issue.

Examples:

* Retry triggered
* Timeout warning
* Validation warning

---

## INFO

Business events.

Examples:

* Application created
* Offer accepted
* Funding requested

---

## DEBUG

Development diagnostics.

Disabled in production.

---

## TRACE

Very detailed execution logs.

Never enabled in production.

---

# 10. Business Event Logging

Important business activities must be logged.

Examples:

```text
ApplicationCreated

ApplicationSubmitted

ApplicationApproved

ApplicationDeclined

OfferAccepted

FundingCompleted
```

---

Example:

```json
{
 "eventType":"ApplicationSubmitted",
 "applicationId":"APP10001",
 "status":"SUBMITTED"
}
```

---

# 11. API Logging

Log:

Request received

Response completed

Processing time

Error response

---

Example:

```json
{
 "api":"/applications",
 "method":"POST",
 "status":201,
 "durationMs":250
}
```

---

# 12. Integration Logging

Log:

External request

External response

Timeouts

Retries

Circuit breaker events

---

Example:

```json
{
 "integration":"CreditPlatform",
 "operation":"CreditEvaluation",
 "durationMs":450,
 "status":"SUCCESS"
}
```

---

# 13. Event Logging

Log:

Event published

Event consumed

Event failed

DLQ events

---

Example:

```json
{
 "event":"ApplicationSubmitted",
 "status":"PUBLISHED"
}
```

---

# 14. Security Logging

Log:

Authentication success

Authentication failure

Authorization failure

Privilege changes

Configuration changes

---

Never log:

Passwords

Tokens

Secrets

---

# 15. PII Logging Rules

Never log:

```text
SSN

DOB

Bank Account

Password

Token

Secret
```

---

# 16. Data Masking

Allowed:

```text
XXX-XX-1234
```

---

Email:

```text
john****@mail.com
```

---

Phone:

```text
XXX-XXX-4567
```

---

# 17. MDC Standards

Use Mapped Diagnostic Context (MDC).

Required entries:

```java
correlationId

traceId

applicationId
```

---

Example:

```java
MDC.put("correlationId", correlationId);
```

---

# 18. Spring Boot Logging Pattern

Required context:

```text
timestamp

level

service

correlationId

traceId

message
```

---

# 19. Log Retention

Application logs:

Minimum operational retention.

---

Audit logs:

Long-term retention according to enterprise policy.

---

Security logs:

Retained according to security policy.

---

# 20. Log Aggregation

All services send logs to:

Centralized logging platform.

Capabilities:

* Search
* Correlation
* Dashboarding
* Alerting

---

# 21. Monitoring Integration

Logs support:

Alerts

Dashboards

Incident investigations

Operational reporting

---

# 22. Performance Guidelines

Avoid:

Excessive logging

Large payload logging

Object serialization logging

---

Bad:

```java
log.info(application.toString());
```

---

Good:

```java
log.info("Application submitted id={}", applicationId);
```

---

# 23. Exception Logging

Log once.

Avoid duplicate logging.

---

Bad:

```text
Controller logs error

Service logs error

Repository logs error
```

---

Good:

```text
Exception handler logs error
```

---

# 24. Audit Logging

Audit records:

Who

What

When

Where

Why

---

Example:

```json
{
 "userId":"USR100",
 "action":"OfferAccepted",
 "applicationId":"APP10001"
}
```

---

# 25. Production Support Standards

Every incident must be traceable using:

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

# 26. Logging Checklist

Before production:

☐ Structured logging enabled

☐ Correlation IDs propagated

☐ Trace IDs enabled

☐ PII masking verified

☐ Security logging enabled

☐ Audit logging enabled

☐ Dashboards configured

☐ Alerts configured

---

# Related Documents

001-service-design-guidelines.md

003-error-handling-standard.md

005-testing-strategy.md

006-coding-standard.md

007-security-architecture.md

008-observability-architecture.md
