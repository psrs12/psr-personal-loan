# acquisition/invitation-to-apply/008-invitation-to-apply-event-spec.md

# Personal Loan Acquisition Platform

# Invitation To Apply Event Specification

Version: 1.0

Status: Draft

Owner: Acquisition Domain

---

# 1. Purpose

This document defines event contracts produced by the Invitation To Apply (ITA) service.

The events enable:

* Loose coupling between services
* Asynchronous processing
* Audit traceability
* Downstream workflow initiation

---

# 2. Event Architecture

```text
                 ITA Service


                      |

                      |

              Event Publisher


                      |

                      |

              Event Streaming Platform


                      |

        +-------------+--------------+

        |             |              |

 Application     Audit Service   Analytics

 Service
```

---

# 3. Event Platform

Technology:

```text
Apache Kafka
```

---

# 4. Event Principles

## EVT-001 Immutable Events

Events represent completed business facts.

Example:

Good:

```text
InvitationValidated
```

Not:

```text
ValidateInvitation
```

---

## EVT-002 Consumer Independence

Consumers must not depend on ITA internal implementation.

---

## EVT-003 Schema Versioning

All events are versioned.

Example:

```text
InvitationValidated.v1
```

---

# 5. Event Ownership

ITA owns publishing:

* InvitationValidated
* OfferRetrieved
* ApplicationCreated
* InvitationExpired
* InvitationRejected

---

ITA does NOT publish:

* FraudDecision
* CreditDecision
* ApprovalDecision

Those belong to respective domains.

---

# 6. Topic Standards

Kafka topic naming:

```text
<domain>.<event>
```

---

Examples:

```text
acquisition.invitation.validated

acquisition.invitation.offer-retrieved

acquisition.application.created
```

---

# 7. Common Event Envelope

All events follow:

```json
{
 "eventId":"uuid",

 "eventType":"InvitationValidated",

 "eventVersion":"1.0",

 "eventTime":"2026-06-24T10:00:00Z",

 "source":"ita-service",

 "correlationId":"abc123",

 "payload":{}
}
```

---

# 8. InvitationValidated Event

## Purpose

Published when invitation validation succeeds.

---

Topic:

```text
acquisition.invitation.validated
```

---

Producer:

ITA Service

---

Consumers:

* Application Service
* Audit Service
* Analytics

---

# Payload

```json
{
 "invitationId":"INV12345",

 "offerId":"OFF98765",

 "status":"VALID",

 "validatedAt":"2026-06-24T10:00:00Z"
}
```

---

# Schema

```text
invitationId

offerId

status

validatedAt
```

---

# 9. OfferRetrieved Event

## Purpose

Published after offer information is successfully retrieved.

---

Topic:

```text
acquisition.invitation.offer-retrieved
```

---

Payload:

```json
{
 "invitationId":"INV12345",

 "offerId":"OFF98765",

 "offerStatus":"ACTIVE",

 "expirationDate":"2026-12-31"
}
```

---

# Consumers

* Application UI workflow
* Audit
* Reporting

---

# 10. ApplicationCreated Event

## Purpose

Indicates invitation converted into application.

---

Topic:

```text
acquisition.application.created
```

---

Producer:

ITA Service

---

Consumers:

* Application Service
* Customer Workflow
* Notification Service

---

Payload:

```json
{
 "applicationId":"APP12345",

 "invitationId":"INV12345",

 "offerId":"OFF98765",

 "status":"STARTED",

 "createdAt":"2026-06-24T10:00:00Z"
}
```

---

# 11. InvitationExpired Event

## Purpose

Published when invitation cannot continue because offer expired.

---

Topic:

```text
acquisition.invitation.expired
```

---

Payload:

```json
{
 "invitationId":"INV12345",

 "offerId":"OFF98765",

 "expirationDate":"2026-06-20"
}
```

---

# 12. InvitationRejected Event

## Purpose

Published when invitation fails validation.

---

Topic:

```text
acquisition.invitation.rejected
```

---

Payload:

```json
{
 "invitationId":"INV12345",

 "reason":"ALREADY_USED",

 "rejectedAt":"2026-06-24T10:00:00Z"
}
```

---

# 13. Event Headers

Every Kafka message requires:

| Header        | Purpose           |
| ------------- | ----------------- |
| eventId       | Unique identifier |
| eventType     | Event name        |
| version       | Schema version    |
| correlationId | Request tracing   |
| source        | Producer          |

---

# 14. Partition Strategy

Kafka partition key:

```text
applicationId
```

For ITA-only events:

```text
invitationId
```

---

Purpose:

Maintain ordering per business entity.

---

# 15. Ordering Requirement

Events for same invitation must preserve order.

Example:

Correct:

```text
InvitationValidated

OfferRetrieved

ApplicationCreated
```

---

Incorrect:

```text
ApplicationCreated

InvitationValidated
```

---

# 16. Delivery Guarantee

Pattern:

At-least-once delivery

---

Consumers must support:

* Duplicate events
* Replay
* Retry

---

# 17. Idempotency

Every consumer must store:

```text
eventId
```

---

Duplicate handling:

```text
If eventId already processed

Ignore event
```

---

# 18. Retry Strategy

Failed consumption:

Retry topic:

```text
acquisition.invitation.retry
```

---

After retries:

Dead Letter Queue:

```text
acquisition.invitation.dlq
```

---

# 19. Dead Letter Event

Example:

```json
{
 "originalEvent":"ApplicationCreated",

 "failureReason":"Processing Error",

 "failedAt":"2026-06-24T10:00:00Z"
}
```

---

# 20. Schema Evolution

Rules:

Backward compatible changes only.

Allowed:

* Add optional fields

Not allowed:

* Rename fields
* Remove fields
* Change meaning

---

# 21. Event Security

Events must not contain:

* Password
* Token
* Secrets

---

PII restrictions:

Avoid sending:

* SSN
* DOB
* Full address

unless required.

---

# 22. Audit Correlation

Every event contains:

```text
correlationId
```

Used for:

```text
Request

↓

API Logs

↓

Kafka Event

↓

Consumer Logs
```

---

# 23. Monitoring

Track:

Producer:

* Publish success
* Publish failures

Consumer:

* Processing latency
* Error rate
* DLQ count

---

# 24. Testing Requirements

Required:

## Producer Tests

Verify:

* Event generated
* Schema valid

## Consumer Contract Tests

Verify:

* Payload compatibility
* Version handling

## Integration Tests

Verify:

* Kafka publishing
* Consumption flow

---

# 25. Event Acceptance Criteria

☐ Event envelope standardized

☐ Topics defined

☐ Schemas versioned

☐ Correlation supported

☐ Retry supported

☐ DLQ supported

☐ Consumers documented

---

# Related Documents

* 001-invitation-to-apply-spec.md
* 002-state-machine.md
* 003-api-spec.md
* 005-integration-spec.md
* 006-service-design.md
* 007-openapi.yaml
* 008-event-architecture.md
ß