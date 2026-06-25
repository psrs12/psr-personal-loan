# architecture/006-integration-patterns.md

# Personal Loan Acquisition Platform

## Integration Patterns Specification

Version: 1.0

Status: Draft

Owner: Enterprise Architecture

---

# 1. Purpose

This document defines integration patterns used by the Personal Loan Acquisition Platform.

The objective is to provide:

* Reliable enterprise integration
* Loose coupling
* Resilience
* Consistent communication patterns
* Failure handling

---

# 2. Integration Principles

## INT-001 API First Integration

Systems integrate using:

* REST APIs
* Events

No direct database integration.

---

## INT-002 Loose Coupling

Business services should not depend on enterprise implementation details.

---

## INT-003 Failure Isolation

Failures in external systems must not bring down the acquisition platform.

---

## INT-004 Observable Integration

All integrations must support:

* Logging
* Metrics
* Tracing
* Correlation IDs

---

# 3. Integration Architecture

```text
                 Acquisition Platform


                       |
                       |

        +-------------------------------+
        | Integration Layer             |
        +-------------------------------+

          |          |          |

          v          v          v


     REST API    Events     File/API Batch


          |          |          |


          v          v          v


Enterprise Platforms

Offer Management

Fraud Platform

Credit Management

Decision Platform

Identity Platform

Funding Platform
```

---

# 4. Integration Types

## Synchronous Integration

Used when immediate response is required.

Examples:

* Validate Invitation
* Retrieve Offer
* Submit Decision Request
* Retrieve Application Status

Pattern:

REST API

---

## Asynchronous Integration

Used for long-running processes.

Examples:

* Fraud evaluation
* Credit evaluation
* Funding completion

Pattern:

Event driven

---

# 5. REST Integration Standard

## Request Flow

```text
Service A

 |

API Gateway / Integration Layer

 |

Service B
```

---

# 6. REST Timeout Standard

Default timeout:

30 seconds

External dependency timeout:

10-30 seconds

Long-running operations:

Use async pattern

---

# 7. Retry Pattern

Retry only transient failures.

Retry:

* Network failure
* Timeout
* HTTP 503
* HTTP 429

Do not retry:

* Validation errors
* Authentication failures
* Business rejection

---

# 8. Retry Strategy

Example:

```text
Attempt 1

wait 1 sec

Attempt 2

wait 5 sec

Attempt 3

wait 15 sec

Failure

↓

Dead Letter / Exception Flow
```

---

# 9. Circuit Breaker Pattern

Purpose:

Prevent cascading failures.

States:

```text
CLOSED

 |

Failure threshold reached

 |

OPEN

 |

Recovery test

 |

HALF OPEN

 |

CLOSED
```

---

# 10. Bulkhead Pattern

Purpose:

Isolate failures.

Example:

Fraud Platform outage should not impact:

* Application viewing
* Document upload
* Customer tracking

---

# 11. Adapter Pattern

Enterprise systems are accessed through adapters.

Example:

```text
Application Service

        |

Fraud Adapter

        |

Enterprise Fraud API
```

---

# 12. Anti-Corruption Layer

Purpose:

Protect acquisition domain from external models.

Example:

External:

```json
{
 "fraudScore":90
}
```

Internal:

```json
{
 "verificationStatus":"COMPLETED"
}
```

---

# 13. Enterprise Integration Boundaries

## Offer Management

Integration:

REST

Used for:

* Invitation validation
* Offer retrieval

Owned by:

Offer Management

---

## Fraud Platform

Integration:

REST + Events

Used for:

* Fraud verification request
* Fraud completion

Owned by:

Enterprise Fraud

---

## Credit Management

Integration:

REST + Events

Used for:

* Credit evaluation

Owned by:

Enterprise Credit

---

## Decision Platform

Integration:

REST

Used for:

* Decision request
* Decision response

Owned by:

Decision Platform

---

## Identity Platform

Integration:

REST + Events

Used for:

* Verification

---

## Funding Platform

Integration:

REST + Events

Used for:

* Funding request
* Funding status

---

# 14. Request Correlation

Every integration call must include:

```text
X-Correlation-ID

X-Request-ID

Application-ID
```

---

Example:

```http
X-Correlation-ID: abc123

Application-ID: APP10001
```

---

# 15. Data Mapping

Integration services perform:

* Transformation
* Validation
* Mapping

Example:

Enterprise:

```text
customerDOB
```

Internal:

```text
dateOfBirth
```

---

# 16. Error Handling

External error mapping:

Enterprise:

```json
{
 "error":"CREDIT_DOWN"
}
```

Mapped to:

```json
{
 "errorCode":"CRD-503",
 "message":"Credit service unavailable"
}
```

---

# 17. Async Integration Pattern

Example:

Fraud Flow

```text
Application Service

 |

FraudRequested Event

 |

Fraud Service

 |

Enterprise Fraud API

 |

FraudCompleted Event

 |

Application Updated
```

---

# 18. Outbox Pattern

Required for reliable event publishing.

Flow:

```text
Business Update

        |

Database Commit

        |

Outbox Table

        |

Event Publisher

        |

Event Platform
```

---

# 19. Idempotency

Required for:

* POST operations
* Event consumers

Example:

Funding request

Header:

```text
Idempotency-Key
```

---

# 20. Compensation

For failed workflows:

Use compensation actions.

Example:

Decision approved but funding failed.

Action:

Move application to:

FUNDING_PENDING

---

# 21. Monitoring

Track:

Integration latency

Failure rate

Retry count

Circuit breaker status

Event lag

---

# 22. Security

All integrations require:

TLS

OAuth2

Service identity

Secret management

Certificate management

---

# 23. Testing Strategy

Required:

Contract testing

API testing

Integration testing

Failure testing

Performance testing

---

# 24. Related Documents

000-architecture-overview.md

001-logical-architecture.md

002-microservice-boundaries.md

004-api-standards.md

005-event-driven-architecture.md

007-security-architecture.md

008-observability-architecture.md
