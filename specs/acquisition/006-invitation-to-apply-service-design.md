# acquisition/invitation-to-apply/006-invitation-to-apply-service-design.md

# Personal Loan Acquisition Platform

# Invitation To Apply Service Design Specification

Version: 1.0

Status: Draft

Owner: Acquisition Domain

---

# 1. Purpose

This document defines the technical service design for the Invitation To Apply (ITA) microservice.

The design provides:

* Component structure
* Responsibilities
* Domain model
* Application workflow
* Integration boundaries
* Implementation guidance

---

# 2. Architecture Style

The service follows:

* Domain Driven Design (DDD)
* Hexagonal Architecture
* REST-based integration
* Event-driven support

---

# 3. Service Context

```text
                Acquisition Platform


                 ITA Service

        +---------------------------+
        |                           |
        |  Invitation Domain        |
        |                           |
        +---------------------------+

             |              |

             |              |

     Offer Management     Application Service
        External             Internal
```

---

# 4. Service Responsibilities

## ITA Owns

* Invitation lifecycle
* Invitation validation
* Offer reference tracking
* Customer prefill workflow
* Application initiation

---

## ITA Does Not Own

* Offer generation
* Pricing
* Credit assessment
* Fraud assessment
* Decisioning
* Funding

---

# 5. High Level Component Design

```text
API Layer

    |

Application Layer

    |

Domain Layer

    |

Infrastructure Layer

```

---

# 6. Package Structure

```text
com.company.loan.acquisition.ita

├── api
│
├── application
│
├── domain
│
├── infrastructure
│
└── common
```

---

# 7. API Layer

Responsibilities:

* HTTP handling
* Request validation
* Response mapping
* Security enforcement

Package:

```text
api
```

---

Components:

```
InvitationController
ApplicationStartController
```

---

Example:

```java
@RestController
@RequestMapping("/api/v1/invitations")
class InvitationController
{

}
```

---

# 8. Application Layer

Purpose:

Orchestrates business workflows.

Package:

```text
application
```

---

Components:

```
InvitationApplicationService

ApplicationStartService

OfferRetrievalService
```

---

Responsibilities:

* Transaction boundaries
* Workflow coordination
* Domain invocation

---

# 9. Domain Layer

Contains business logic.

Package:

```text
domain
```

---

## Entities

```
Invitation

InvitationValidation

CustomerSnapshot
```

---

## Value Objects

```
InvitationId

OfferId

ApplicationId
```

---

## Domain Services

```
InvitationValidator

InvitationStateManager
```

---

# 10. Invitation Entity Design

```java
class Invitation {

 InvitationId id;

 String externalInvitationId;

 OfferId offerId;

 InvitationStatus status;

 ApplicationId applicationId;


 void validate();

 void expire();

 void markApplicationCreated();

}
```

---

# 11. State Management

States:

```text
CREATED

VALIDATING

VALIDATED

OFFER_RETRIEVED

READY_TO_APPLY

APPLICATION_CREATED

INVALID

EXPIRED

REJECTED
```

---

State changes only through domain methods.

Bad:

```java
invitation.status = VALIDATED;
```

Good:

```java
invitation.validate();
```

---

# 12. Workflow Design

## Validate Invitation Flow

```text
Controller

 |

Application Service

 |

Invitation Domain

 |

Offer Adapter

 |

Offer Management

```

---

# 13. Application Creation Flow

```text
Customer

 |

ITA Service

 |

Validate Invitation

 |

Retrieve Offer

 |

Create Application

 |

Return Application ID

```

---

# 14. Infrastructure Layer

Responsibilities:

External communication

Database access

Messaging

---

Package:

```text
infrastructure
```

---

Components:

```
OfferManagementClient

InvitationRepository

EventPublisher
```

---

# 15. Offer Management Adapter

Purpose:

Hide external API dependency.

---

Interface:

```java
interface OfferService {

 Offer retrieveOffer(
    InvitationId id);

}
```

---

Implementation:

```java
class OfferManagementClient
implements OfferService

```

---

# 16. Repository Design

Interface:

```java
interface InvitationRepository
{

 Optional<Invitation>
 findByExternalId(String id);

 void save(Invitation invitation);

}
```

---

Implementation:

```text
JpaInvitationRepository
```

---

# 17. DTO Design

API DTOs:

```
InvitationRequest

InvitationResponse

ApplicationStartRequest

ApplicationStartResponse
```

---

Domain models are never exposed.

---

# 18. Mapping Layer

Use:

MapStruct

---

Example:

```java
InvitationMapper
```

---

Mapping:

API DTO

↓

Domain

↓

Response DTO

---

# 19. External Integration Flow

```text
ITA Service

 |

OfferService Interface

 |

OfferManagementClient

 |

REST API

 |

Offer Management

```

---

# 20. Error Handling Design

Exceptions:

```
InvitationNotFoundException

ExpiredOfferException

InvitationAlreadyUsedException

OfferUnavailableException
```

---

Global handler:

```text
RestExceptionHandler
```

---

# 21. Logging Design

Every request contains:

```
correlationId

traceId

invitationId

applicationId
```

---

Example:

```java
log.info(
"Invitation validated {}",
invitationId
);
```

---

# 22. Transaction Boundaries

Validate invitation:

Transactional

---

Create application:

Transactional

---

External API calls:

Outside DB transaction where possible.

---

# 23. Resilience Design

Offer Management calls use:

* Timeout
* Retry
* Circuit breaker

---

Example:

```java
@CircuitBreaker(
 name="offerService"
)
```

---

# 24. Event Design

ITA publishes:

```text
InvitationValidated

OfferRetrieved

ApplicationCreated
```

---

Example:

```json
{
 "eventType":"InvitationValidated",
 "invitationId":"INV123"
}
```

---

# 25. Security Design

API Security:

OAuth2

---

Authorization:

```
PROSPECT

APPLICATION_CLIENT

INTERNAL_SERVICE
```

---

# 26. Testing Design

Required:

Unit Tests

Integration Tests

Contract Tests

Component Tests

---

Coverage target:

```
Domain >= 90%

Service >= 80%

```

---

# 27. Configuration

Example:

```yaml
offer:
  service:
    url: https://offer/api

resilience:
 retry:
  maxAttempts: 3
```

---

# 28. Deployment Unit

Artifact:

```text
ita-service.jar
```

Container:

```text
ita-service:1.0
```

---

# 29. Observability

Expose:

Spring Actuator

Metrics:

```
invitation.validation.count

offer.lookup.latency

application.creation.count
```

---

# 30. Definition of Done

Service complete when:

☐ APIs implemented

☐ Domain rules implemented

☐ Database created

☐ Offer integration completed

☐ Tests completed

☐ Security enabled

☐ Logging enabled

☐ Deployment ready

---

# Related Documents

* 001-invitation-to-apply-spec.md
* 002-state-machine.md
* 003-api-spec.md
* 004-data-model.md
* 005-integration-spec.md
* 006-coding-standard.md
* 005-testing-strategy.md
