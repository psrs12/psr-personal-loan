# design/001-service-design-guidelines.md

# Personal Loan Acquisition Platform

## Service Design Guidelines

Version: 1.0

Status: Draft

Owner: Application Architecture

---

# 1. Purpose

This document defines application service design standards for the Personal Loan Acquisition Platform.

It establishes:

* Service structure
* Layering approach
* Domain design
* API implementation patterns
* Transaction boundaries
* Dependency management
* Code organization

---

# 2. Design Principles

## SD-001 Domain First

Business capability drives service design.

Do not design services around:

* Database tables
* Technical layers
* CRUD operations

---

## SD-002 Separation of Concerns

Each layer has a clear responsibility.

---

## SD-003 Business Logic Isolation

Business rules must not exist in:

* Controllers
* Repositories
* External adapters

---

## SD-004 Testability

Every component must be independently testable.

---

# 3. Recommended Technology Stack

## Backend

Java

Spring Boot

---

## Frameworks

Spring Web

Spring Data

Spring Security

Spring Validation

Spring Cloud

---

## Persistence

Relational Database

Migration framework

---

## Messaging

Event streaming platform

---

## Testing

JUnit

Mockito

Testcontainers

Contract testing framework

---

# 4. Service Architecture Pattern

Each service follows:

```text id="s9e7vu"
Controller Layer

        |

Application Layer

        |

Domain Layer

        |

Infrastructure Layer
```

---

# 5. Package Structure

Standard:

```text id="k8v6j2"
com.company.loan.application


├── api
│   ├── controller
│   ├── request
│   ├── response
│   └── mapper
│

├── application
│   ├── service
│   ├── command
│   ├── query
│   └── workflow
│

├── domain
│   ├── model
│   ├── entity
│   ├── valueobject
│   ├── rule
│   └── event
│

├── infrastructure
│   ├── repository
│   ├── client
│   ├── messaging
│   └── configuration
│

└── common
    ├── exception
    ├── security
    └── logging
```

---

# 6. Controller Layer Guidelines

Responsibilities:

* Receive HTTP requests
* Validate input
* Convert DTOs
* Call application service

Controllers should NOT:

* Contain business logic
* Access database
* Call external systems directly

---

Example:

```java id="h3e6jq"
@PostMapping("/applications")
public ApplicationResponse create(
        @Valid @RequestBody CreateApplicationRequest request)
{
    return applicationService.create(request);
}
```

---

# 7. Application Layer

Purpose:

Orchestrates business operations.

Responsibilities:

* Workflow execution
* Transaction boundaries
* Calling domain logic
* Calling integrations

Example:

```text id="l6fh0v"
Create Application

 |

Validate

 |

Create Domain Object

 |

Save

 |

Publish Event
```

---

# 8. Domain Layer

Contains business meaning.

Includes:

Entities

Value Objects

Domain Rules

Domain Events

---

Example:

Application Entity:

```java
class Application {

 ApplicationId id;

 ApplicationStatus status;

 void submit(){

    validate();

    status = SUBMITTED;

 }

}
```

---

# 9. Entity Design

Entities:

Have identity.

Contain behavior.

Protect invariants.

Avoid:

Anemic models.

---

Bad:

```java
application.setStatus()
```

Good:

```java
application.submit()
```

---

# 10. Value Objects

Use value objects for concepts.

Examples:

Money

Address

ApplicationId

Email

---

Example:

```java
class Money {

 BigDecimal amount;

 Currency currency;

}
```

---

# 11. Repository Guidelines

Repository responsibility:

Persistence only.

Example:

```java
interface ApplicationRepository {

 Optional<Application>
 findById(ApplicationId id);

 void save(Application application);

}
```

---

Repository should NOT:

* Apply business rules
* Call APIs

---

# 12. External Integration Design

Never call enterprise systems directly from domain.

Use adapters.

Example:

```text id="d5c44m"
Application Service

        |

Fraud Adapter

        |

Fraud Platform
```

---

# 13. Client Package

External clients:

```text
infrastructure/client
```

Example:

```java
FraudClient

CreditClient

OfferClient
```

---

# 14. API Client Rules

Every client must support:

Timeout

Retry

Circuit breaker

Logging

Metrics

---

# 15. Transaction Boundaries

Transactions belong in:

Application Layer

Example:

```java
@Transactional
createApplication()
```

---

Do not create:

Distributed transactions.

---

# 16. Event Publishing

Use:

Outbox Pattern

Flow:

```text id="u0j5wh"
Save Entity

        |

Save Event

        |

Publish Event
```

---

# 17. Exception Handling

Never expose internal exceptions.

Use business exceptions.

Example:

```java
ApplicationNotFoundException
```

---

# 18. DTO Guidelines

API models are separate.

Do not expose:

Entity objects

Database models

---

Example:

Entity:

Application

DTO:

ApplicationResponse

---

# 19. Validation

Validation levels:

## API Validation

Required fields

Format

---

## Business Validation

Rules

State transitions

---

Example:

Cannot submit:

Already cancelled application

---

# 20. State Management

Use explicit states.

Example:

```text
STARTED

SUBMITTED

UNDER_REVIEW

APPROVED

DECLINED

FUNDED
```

---

# 21. Configuration

Externalize:

URLs

Timeouts

Feature flags

Limits

Never hardcode.

---

# 22. Feature Flags

Use for:

Gradual rollout

Testing

Business changes

---

# 23. Logging Guidelines

Every request must include:

applicationId

correlationId

traceId

Never log:

PII

Secrets

Tokens

---

# 24. Testing Requirements

Every service requires:

Unit tests

Integration tests

Contract tests

---

# 25. Code Quality

Required:

Static analysis

Code review

Dependency scanning

---

# 26. Performance Guidelines

Avoid:

N+1 queries

Large payloads

Blocking calls

Use:

Pagination

Caching

Async processing

---

# 27. API Documentation

Every service requires:

OpenAPI specification

Examples

Error models

---

# 28. Service Checklist

Before production:

☐ API documented

☐ Security enabled

☐ Logging enabled

☐ Metrics enabled

☐ Health checks enabled

☐ Tests completed

☐ Deployment configured

---

# Related Documents

002-database-design-guidelines.md

003-error-handling-standard.md

004-logging-standard.md

005-testing-strategy.md

006-coding-standard.md
