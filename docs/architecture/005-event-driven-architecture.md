# architecture/005-event-driven-architecture.md

# Personal Loan Acquisition Platform

## Event Driven Architecture Specification

Version: 1.0

Status: Draft

Owner: Enterprise Architecture

---

# 1. Purpose

This document defines event-driven architecture standards for the Personal Loan Acquisition Platform.

The objective is to enable:

* Loose coupling
* Asynchronous processing
* Scalable workflows
* Reliable integration
* Auditability

---

# 2. Event Architecture Principles

## EA-001 Event Ownership

The service that owns the business capability owns the event.

---

## EA-002 Events Represent Business Facts

Events describe something that already happened.

Example:

Correct:

ApplicationSubmitted

Incorrect:

SubmitApplication

---

## EA-003 Consumers Must Be Independent

Event producers must not know consumers.

---

## EA-004 Events Are Immutable

Events cannot be modified after publishing.

---

## EA-005 Events Are Versioned

Schema changes require versioning.

---

# 3. Event Architecture Overview

```text id="p2w4vu"
Application Service

        |
        |
        v

+----------------------+
|   Event Platform     |
|                      |
|  Event Topics        |
+----------------------+

        |
        |
        +----------------+
        |                |
        v                v

Fraud Service      Credit Service

        |
        v

Decision Service

        |
        v

Funding Service
```

---

# 4. Event Platform

The platform provides:

* Event publishing
* Event subscription
* Delivery guarantee
* Replay capability
* Dead letter handling

Technology:

Enterprise event streaming platform

Example:

Kafka compatible platform

---

# 5. Event Naming Standard

Format:

```text
<Domain><BusinessAction>
```

Examples:

ApplicationCreated

ApplicationSubmitted

CreditEvaluationCompleted

DecisionCompleted

FundingCompleted

---

# 6. Event Categories

## Application Events

Owned by:

Application Service

Events:

ApplicationCreated

ApplicationStarted

ApplicationUpdated

ApplicationSubmitted

ApplicationCancelled

ApplicationCompleted

---

## Identity Events

Owned by:

Identity Orchestration Service

Events:

IdentityVerificationRequested

IdentityVerificationCompleted

IdentityVerificationFailed

---

## Fraud Events

Owned by:

Fraud Orchestration Service

Events:

FraudVerificationRequested

FraudVerificationCompleted

FraudReviewRequired

---

## Credit Events

Owned by:

Credit Orchestration Service

Events:

CreditEvaluationRequested

CreditEvaluationCompleted

CreditReviewRequired

---

## Decision Events

Owned by:

Decision Orchestration Service

Events:

DecisionRequested

DecisionCompleted

ApplicationApproved

ApplicationDeclined

ApplicationReferred

---

## Offer Events

Owned by:

Offer Acceptance Service

Events:

OfferPresented

OfferAccepted

OfferRejected

---

## Document Events

Owned by:

Document Service

Events:

DocumentRequested

DocumentUploaded

DocumentsCompleted

---

## Funding Events

Owned by:

Funding Orchestration Service

Events:

FundingRequested

FundingCompleted

FundingFailed

---

# 7. Event Schema Standard

All events follow common envelope.

Example:

```json id="72c8qv"
{
 "eventId":"12345",
 "eventType":"ApplicationSubmitted",
 "eventVersion":"1.0",
 "timestamp":"2026-06-24T10:00:00Z",

 "source":"application-service",

 "correlationId":"abc123",

 "payload": {

   "applicationId":"APP123",
   "status":"SUBMITTED"

 }
}
```

---

# 8. Required Event Fields

Every event must contain:

eventId

eventType

eventVersion

timestamp

source

correlationId

payload

---

# 9. Topic Design

Pattern:

```text
<domain>.<event>
```

Example:

```text
application.created

application.submitted

credit.completed

decision.completed

funding.completed
```

---

# 10. Event Flow Example

## Application Submission

```text id="jv3l1t"
Customer

 |

Application Service

 |

ApplicationSubmitted

 |

+----------------+

|                |

Fraud            Credit

Service          Service


 |

Decision Service


 |

DecisionCompleted


 |

Application Updated
```

---

# 11. Delivery Guarantees

Default:

At least once delivery

Consumers must support:

Duplicate events

Retry processing

Idempotency

---

# 12. Idempotent Consumers

Each consumer must store:

eventId

processingStatus

processedTimestamp

Example:

```text id="zv73sw"
event_id

consumer_name

processed_date

status
```

---

# 13. Event Retry Strategy

Retry:

Temporary failures

Example:

Enterprise service unavailable

---

Retry pattern:

```text id="c7u1hv"
Retry 1

↓

Retry 2

↓

Retry 3

↓

Dead Letter Queue
```

---

# 14. Dead Letter Queue

Purpose:

Capture failed messages.

Contains:

Original event

Failure reason

Retry count

Timestamp

---

# 15. Event Ordering

Ordering required for:

Application lifecycle events

Example:

Correct:

ApplicationCreated

ApplicationSubmitted

Incorrect:

ApplicationSubmitted

ApplicationCreated

---

# 16. Event Replay

Supported for:

Recovery

Reprocessing

New consumers

Consumers must be replay safe.

---

# 17. Transaction Pattern

Use:

Outbox Pattern

Example:

```text id="h6cvfr"
Database Transaction

        |

Save Application

        |

Save Event

        |

Event Publisher

        |

Kafka
```

---

# 18. Saga Pattern

Used for long-running workflow.

Example:

Loan Application Processing

```text id="c9xy5k"
Application Submitted

        |

Fraud Check

        |

Credit Check

        |

Decision

        |

Funding
```

---

# 19. Event Security

Required:

Encryption

Authentication

Authorization

Schema validation

---

# 20. Monitoring

Track:

Event publishing rate

Consumer lag

Failed events

Processing time

---

# 21. Event Governance

Every event requires:

Owner

Schema

Version

Documentation

Consumers

---

# 22. Related Documents

000-architecture-overview.md

001-logical-architecture.md

002-microservice-boundaries.md

003-data-architecture.md

004-api-standards.md

006-integration-patterns.md

007-security-architecture.md

008-observability-architecture.md
