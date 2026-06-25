# acquisition/invitation-to-apply/005-invitation-to-apply-integration-spec.md

# Personal Loan Acquisition Platform

# Invitation To Apply Integration Specification

Version: 1.0

Status: Draft

Owner: Acquisition Domain

---

# 1. Purpose

This document defines integration patterns and contracts used by the Invitation To Apply capability.

The purpose is to define:

* External system interactions
* API contracts
* Authentication
* Error handling
* Retry behavior
* Resilience patterns

---

# 2. Integration Overview

```text
Prospect

   |

Acquisition Application

   |

Invitation To Apply Service

   |

Offer Management Platform

   |

Enterprise Systems
```

---

# 3. Integration Scope

## In Scope

* Retrieve offer by invitation
* Validate offer status
* Retrieve applicant prefill data
* Handle external failures

---

## Out Of Scope

ITA does NOT integrate directly with:

* Fraud Decision Platform
* Credit Management Platform
* Decision Platform
* Funding Platform

Those integrations belong to downstream application workflow.

---

# 4. External Systems

## 4.1 Offer Management Platform

System Type:

Enterprise Service

Ownership:

Enterprise Offer Management

Responsibilities:

* Offer creation
* Offer eligibility
* Offer lifecycle
* Customer offer profile

---

# 5. Integration Pattern

Pattern:

Synchronous REST API

Reason:

Customer requires immediate response during application start journey.

---

# 6. Offer Retrieval Flow

```text
Prospect

 |

Enter Invitation ID

 |

ITA Service

 |

Offer Management API

 |

Offer Response

 |

Display Offer

```

---

# 7. Offer Management API Contract

## Endpoint

```http
GET /enterprise/offers/invitations/{invitationId}
```

---

# Request

Path:

```text
invitationId
```

Example:

```text
INV-123456789
```

---

# Request Headers

```http
Authorization: Bearer token

X-Correlation-ID: abc123

X-Request-ID: xyz789
```

---

# Response

HTTP 200

```json
{
 "invitationId":"INV-123456789",

 "offer":{
   "offerId":"OFF-98765",
   "status":"ACTIVE",
   "expirationDate":"2026-12-31"
 },

 "customer":{
   "firstName":"John",
   "lastName":"Smith",

   "address":{
     "line1":"123 Main Street",
     "city":"Phoenix",
     "state":"AZ",
     "postalCode":"85001"
   }
 }
}
```

---

# 8. Response Mapping

Offer Management Field:

```text
offer.offerId
```

maps to:

```text
ITA.offerId
```

---

Customer:

```text
customer.firstName
customer.lastName
customer.address
```

maps to:

```text
Invitation Customer Snapshot
```

---

# 9. Authentication

Authentication:

OAuth 2.0 Client Credentials

Flow:

```text
ITA Service

 |

Client ID + Secret

 |

Authorization Server

 |

Access Token

 |

Offer API
```

---

# 10. Credential Management

Secrets stored in:

Enterprise Vault

Never store:

* Client secret
* Token
* Password

in:

* Code
* Config files
* Database

---

# 11. Timeout Configuration

Default:

Connection timeout:

```text
2 seconds
```

Read timeout:

```text
5 seconds
```

---

# 12. Retry Strategy

Retries allowed for:

* Network timeout
* Connection failure
* HTTP 503
* HTTP 429

---

Retry policy:

```text
Attempt 1

wait

Attempt 2

wait

Attempt 3
```

---

# 13. Retry Not Allowed

Do NOT retry:

HTTP 400

HTTP 401

HTTP 403

HTTP 404

Business validation failures

---

# 14. Circuit Breaker

Required.

Purpose:

Prevent cascading failures.

---

States:

```text
CLOSED

 |

OPEN

 |

HALF_OPEN
```

---

# 15. Circuit Breaker Configuration

Example:

```yaml
failureRateThreshold: 50

waitDuration: 30s

permittedCallsInHalfOpen: 5
```

---

# 16. Failure Handling

## Scenario 1

Offer Not Found

Response:

```text
HTTP 404
```

ITA Response:

```text
ITA-001
```

---

## Scenario 2

Offer Expired

Offer Management:

```text
status=EXPIRED
```

ITA:

```text
ITA-002
```

---

## Scenario 3

Offer Service Down

Offer Management:

```text
HTTP 503
```

ITA:

```text
ITA-004
```

---

# 17. Idempotency

Offer retrieval:

Read-only.

No idempotency required.

---

Application creation:

Requires:

```http
Idempotency-Key
```

---

# 18. Correlation Propagation

Required flow:

```text
Customer Request

 |

correlationId

 |

ITA Service

 |

Offer Management

```

---

Every log must contain:

```text
correlationId
```

---

# 19. Logging Requirements

Log:

Request sent

Response received

Latency

Status

Error code

---

Do NOT log:

Customer PII

Access token

Secrets

---

# 20. Monitoring Metrics

Track:

## Availability

Offer API success rate

---

## Performance

Response time

P95 latency

---

## Reliability

Timeout count

Retry count

Circuit breaker state

---

# 21. Distributed Tracing

Required:

traceId

spanId

Example:

```text
Trace

 |

ITA Service Span

 |

Offer API Span

```

---

# 22. API Versioning

External API:

Versioned.

Example:

```http
/api/v1/offers
```

---

Breaking changes:

New version required.

---

# 23. Contract Testing

Required between:

ITA

and

Offer Management

Validate:

* Request schema
* Response schema
* Error contract

---

# 24. Integration Testing

Required scenarios:

## Happy Path

Invitation → Offer → Application

---

## Failure Paths

Offer unavailable

Expired offer

Invalid invitation

Timeout

---

# 25. Data Consistency

ITA stores:

Reference data only.

Example:

```text
offerId
invitationId
```

---

ITA does NOT duplicate:

Offer pricing rules

Offer calculations

Eligibility logic

---

# 26. Security Requirements

Required:

TLS

OAuth2

Certificate validation

Secret rotation

Audit logging

---

# 27. Operational Support

Support troubleshooting:

Using:

```text
Correlation ID

↓

Trace ID

↓

ITA Logs

↓

Offer Logs
```

---

# 28. Integration Acceptance Criteria

☐ Offer retrieval implemented

☐ OAuth2 configured

☐ Timeout configured

☐ Retry configured

☐ Circuit breaker enabled

☐ Logging implemented

☐ Monitoring enabled

☐ Contract tests created

---

# Related Documents

* 001-invitation-to-apply-spec.md
* 002-invitation-to-apply-state-machine.md
* 003-invitation-to-apply-api-spec.md
* 004-invitation-to-apply-data-model.md
* 003-error-handling-standard.md
* 004-logging-standard.md
* 006-integration-patterns.md
