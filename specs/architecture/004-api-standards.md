# architecture/004-api-standards.md

# Personal Loan Acquisition Platform

## API Standards Specification

Version: 1.0

Status: Draft

Owner: Enterprise Architecture

---

# 1. Purpose

This document defines API design standards for the Personal Loan Acquisition Platform.

The objective is to ensure:

* Consistent API design
* Secure communication
* Predictable error handling
* Easy integration
* Maintainability

---

# 2. API Principles

## API-001 API First

All business capabilities must be exposed through well-defined APIs.

---

## API-002 Contract First

API contracts shall be defined before implementation.

Specifications:

OpenAPI 3.x

---

## API-003 Backward Compatibility

Existing API consumers must not break.

Changes require:

* New version
* Deprecation period

---

## API-004 Secure By Default

All APIs require:

* Authentication
* Authorization
* Encryption

---

## API-005 Observable APIs

Every request must support:

* Correlation tracking
* Logging
* Metrics
* Tracing

---

# 3. API Architecture

```text
Customer / System

        |

        v

+----------------+
| API Gateway    |
+----------------+

        |

        v

+----------------+
| Service API    |
+----------------+

        |

        v

+----------------+
| Service Layer  |
+----------------+
```

---

# 4. API Types

## External APIs

Used by:

* Customer applications
* Partner channels
* Enterprise consumers

Examples:

Create Application

Get Application Status

---

## Internal APIs

Used between acquisition services.

Examples:

Application Service

Document Service

---

## Enterprise Integration APIs

Used to connect enterprise platforms.

Examples:

Offer Management

Credit Platform

Decision Platform

---

# 5. REST Standards

## HTTP Methods

| Method | Usage            |
| ------ | ---------------- |
| GET    | Retrieve         |
| POST   | Create / Execute |
| PUT    | Full Update      |
| PATCH  | Partial Update   |
| DELETE | Remove           |

---

# 6. Resource Naming

Use nouns.

Correct:

```
/applications
```

Incorrect:

```
/createApplication
```

---

Use plural resources.

Example:

```
GET /applications/{applicationId}
```

---

# 7. API Versioning

Version required.

Pattern:

```
/api/v1/applications
```

---

Future:

```
/api/v2/applications
```

---

Version changes required for:

Breaking contract changes

Schema changes

Behavior changes

---

# 8. Request Standards

Every request must include:

## Headers

```text
Authorization

X-Correlation-ID

X-Request-ID

X-Client-ID

Content-Type

Accept
```

---

Example:

```http
POST /api/v1/applications

Authorization: Bearer token

X-Correlation-ID: abc-123
```

---

# 9. Response Standards

Successful response:

```json id="9h3f5f"
{
  "data": {
    "applicationId": "APP123",
    "status": "STARTED"
  },
  "correlationId": "abc-123"
}
```

---

# 10. Pagination Standard

For collections:

Example:

```
GET /applications?page=1&size=20
```

Response:

```json id="3hs3i7"
{
 "items": [],
 "page":1,
 "size":20,
 "total":100
}
```

---

# 11. Error Handling Standard

All APIs use common error model.

---

## Error Response

```json id="0g2p9k"
{
 "error": {
   "code":"APP-001",
   "message":"Application not found",
   "type":"BUSINESS_ERROR"
 },
 "correlationId":"abc123",
 "timestamp":"2026-06-24T10:00:00Z"
}
```

---

# 12. Error Categories

## Validation Error

HTTP 400

Example:

Missing required field

---

## Authentication Error

HTTP 401

Example:

Invalid token

---

## Authorization Error

HTTP 403

Example:

Insufficient permission

---

## Resource Not Found

HTTP 404

Example:

Application missing

---

## Business Error

HTTP 422

Example:

Invalid application state

---

## Conflict

HTTP 409

Example:

Duplicate submission

---

## Service Failure

HTTP 503

Example:

Enterprise service unavailable

---

# 13. Domain Error Codes

Format:

```
<SERVICE>-<NUMBER>
```

Examples:

Application:

```
APP-001
APP-002
```

Invitation:

```
ITA-001
ITA-002
```

Credit:

```
CRD-001
```

---

# 14. API Security

## Authentication

Standard:

OAuth 2.0

OpenID Connect

---

## Service Authentication

Machine-to-machine:

OAuth Client Credentials

---

## User Authentication

Customer:

Authorization Code Flow

---

## Token Requirements

JWT required.

Contains:

subject

roles

scopes

expiry

issuer

---

# 15. Authorization

Authorization model:

RBAC + ABAC

Example roles:

CUSTOMER

UNDERWRITER

OPERATIONS

ADMIN

---

# 16. API Security Controls

Required:

TLS

Input validation

Rate limiting

Threat protection

Schema validation

---

# 17. Idempotency

Required for retryable operations.

Example:

Application creation.

Header:

```
Idempotency-Key
```

---

Example:

```http
POST /applications

Idempotency-Key: abc123
```

---

# 18. Timeout Standards

Default:

30 seconds

Long running operations:

Async processing

Example:

Decision processing

Funding request

---

# 19. Retry Standards

Retry allowed for:

Network failures

Temporary service failures

Retry NOT allowed:

Validation errors

Business errors

---

# 20. API Documentation

All APIs require:

OpenAPI specification

Examples

Error definitions

Security definitions

---

# 21. API Testing

Required:

Contract tests

Integration tests

Security tests

---

# 22. API Monitoring

Track:

Request count

Response time

Error rate

Latency

Availability

---

# 23. API Deprecation

Process:

Announce

Support old version

Migration period

Remove

Minimum notice:

6 months

---

# 24. Related Documents

000-architecture-overview.md

001-logical-architecture.md

002-microservice-boundaries.md

003-data-architecture.md

005-event-driven-architecture.md

006-integration-patterns.md

007-security-architecture.md

008-observability-architecture.md
