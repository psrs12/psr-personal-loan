# architecture/001-logical-architecture.md

# Personal Loan Acquisition Platform

## Logical Architecture Specification

Version: 1.0

Status: Draft

Owner: Enterprise Architecture

---

# 1. Purpose

This document defines the logical architecture of the Personal Loan Acquisition Platform.

It describes:

* Runtime components
* Service interactions
* Integration boundaries
* Data ownership
* Communication patterns
* Request flows

---

# 2. Logical Architecture Overview

The platform follows a layered architecture:

```text
+------------------------------------------------------+
|                  Customer Channels                   |
|                                                      |
| Web Application                                      |
| Mobile Application                                   |
| Partner Channels                                     |
+--------------------------+---------------------------+
                           |
                           v

+------------------------------------------------------+
|                   API Gateway                        |
|                                                      |
| Routing                                             |
| Authentication                                      |
| Rate Limiting                                       |
| Security Policies                                   |
+--------------------------+---------------------------+
                           |
                           v

+------------------------------------------------------+
|             Acquisition Application Layer            |
+------------------------------------------------------+

+----------------+  +----------------+  +-------------+
| Application    |  | Invitation     |  | Offer       |
| Service        |  | Service        |  | Acceptance  |
+----------------+  +----------------+  +-------------+

+----------------+  +----------------+  +-------------+
| Underwriting   |  | Document       |  | Tracking    |
| Service        |  | Service        |  | Service     |
+----------------+  +----------------+  +-------------+

                           |
                           v

+------------------------------------------------------+
|          Enterprise Integration Layer                |
+------------------------------------------------------+

Identity Orchestration

Fraud Orchestration

Credit Orchestration

Decision Orchestration

Funding Orchestration

Notification Orchestration


                           |
                           v

+------------------------------------------------------+
|              Enterprise Platforms                   |
+------------------------------------------------------+

Offer Management

Fraud Platform

Credit Management

Decision Platform

Identity Platform

Document Platform

Funding Platform

Core Loan System
```

---

# 3. Architecture Layers

## 3.1 Channel Layer

Purpose:

Provide customer interaction channels.

Components:

Web UI

Mobile UI

Partner Applications

Responsibilities:

* Capture customer input
* Display application progress
* Present offers
* Collect acceptance

Does not contain business logic.

---

# 3.2 API Gateway Layer

Purpose:

Single entry point for all APIs.

Responsibilities:

* Request routing
* Authentication enforcement
* Authorization checks
* Rate limiting
* Threat protection
* Request logging

---

# 3.3 Acquisition Domain Layer

This is the core business layer.

Owned by Personal Loan Acquisition.

---

## Application Service

Responsibilities:

* Application lifecycle
* State management
* Applicant data
* Submission workflow

Owns:

Application database

---

## Invitation Service

Responsibilities:

* Invitation validation workflow
* Offer retrieval orchestration
* Application initialization

Depends on:

Enterprise Offer Management

---

## Offer Acceptance Service

Responsibilities:

* Display offer
* Capture acceptance
* Capture consent

Does not own:

Offer creation

Pricing

---

## Document Service

Responsibilities:

* Document requests
* Document tracking
* Document status

Does not own:

Document repository

---

## Underwriting Service

Responsibilities:

* Manual review workflow
* Work assignment
* Conditions

---

## Application Tracking Service

Responsibilities:

* Customer status view
* Application timeline
* Progress tracking

---

# 4. Enterprise Integration Layer

This layer isolates enterprise dependencies.

Purpose:

Prevent business services from directly coupling with external platforms.

---

# Identity Orchestration Service

Integrates with:

Enterprise Identity Platform

Responsibilities:

* Submit verification request
* Receive result
* Update workflow

---

# Fraud Orchestration Service

Integrates with:

Enterprise Fraud Platform

Responsibilities:

* Fraud request
* Fraud response handling
* Personal loan fraud workflow rules

Does not own:

Fraud decision

Fraud models

---

# Credit Orchestration Service

Integrates with:

Enterprise Credit Management

Responsibilities:

* Credit request
* Response processing
* Credit workflow

Does not own:

Credit models

Credit scores

Credit data

---

# Decision Orchestration Service

Integrates with:

Enterprise Decision Platform

Responsibilities:

* Submit decision request
* Receive decision
* Update application state

Does not own:

Decision rules

Risk models

---

# Funding Orchestration Service

Integrates with:

Funding Platform

Responsibilities:

* Submit funding request
* Track funding status

Does not own:

Loan disbursement

---

# Notification Orchestration Service

Integrates with:

Notification Platform

Responsibilities:

* Create notification requests
* Track delivery status

---

# 5. Data Architecture

## Application Database

Owned by:

Application Service

Stores:

Application

Applicant Data

Application Status

Workflow State

Audit Reference

---

## Service Databases

Each service owns:

Own schema

Own migrations

Own persistence model

Example:

```text
application_db

invitation_db

document_db

underwriting_db

tracking_db
```

---

# 6. Communication Architecture

## Synchronous Communication

Used when immediate response required.

Examples:

Invitation validation

Application retrieval

Offer acceptance

Protocol:

REST API

HTTPS

---

## Asynchronous Communication

Used for workflow events.

Examples:

ApplicationSubmitted

CreditCompleted

DecisionCompleted

FundingCompleted

Technology:

Event streaming platform

---

# 7. Request Flow Examples

## Invitation Journey

```text
Customer

  |

API Gateway

  |

Invitation Service

  |

Offer Management

  |

Application Service

  |

Application Created
```

---

## Application Submission Flow

```text
Customer

 |

Application Service

 |

Publish ApplicationSubmitted Event

 |

+----------------+
|                |
v                v

Fraud          Credit

 |

Decision Service

 |

Application Updated
```

---

# 8. Transaction Boundaries

Each service owns local transactions.

Distributed transactions are avoided.

Pattern:

Saga orchestration

---

Example:

Application Submission

```text
Application Submitted

        |

Fraud Check

        |

Credit Check

        |

Decision

        |

Update Application
```

---

# 9. Resilience Architecture

Required patterns:

## Timeout

Prevent long waits.

---

## Retry

For transient failures.

---

## Circuit Breaker

Prevent cascading failures.

---

## Bulkhead

Isolate failures.

---

## Dead Letter Queue

Handle failed events.

---

# 10. Security Boundary

Security enforced at:

API Gateway

Service Layer

Database Layer

Integration Layer

Controls:

OAuth2

JWT

mTLS

RBAC

Encryption

Audit

---

# 11. Observability Boundary

Every service exposes:

Health endpoint

Metrics

Logs

Traces

Required correlation:

applicationId

transactionId

correlationId

---

# 12. Deployment View

Logical deployment:

```text
Kubernetes Cluster

 |
 +-- API Gateway

 |
 +-- Acquisition Services

 |
 +-- Integration Services

 |
 +-- Observability Stack

 |
 +-- Security Components
```

---

# 13. Related Documents

000-architecture-overview.md

002-microservice-boundaries.md

003-data-architecture.md

004-api-standards.md

005-event-driven-architecture.md

006-integration-patterns.md

007-security-architecture.md

008-observability-architecture.md
