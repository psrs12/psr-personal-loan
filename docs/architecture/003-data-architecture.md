# architecture/003-data-architecture.md

# Personal Loan Acquisition Platform

## Data Architecture Specification

Version: 1.0

Status: Draft

Owner: Enterprise Architecture

---

# 1. Purpose

This document defines the data architecture strategy for the Personal Loan Acquisition Platform.

It establishes:

* Data ownership boundaries
* Data storage strategy
* Database architecture
* Data security controls
* Data lifecycle management
* Data integration patterns

---

# 2. Data Architecture Principles

## DA-001 Data Ownership

Every service owns and manages its business data.

A service shall not directly access another service database.

---

## DA-002 System Of Record

Each data element has one authoritative owner.

Example:

| Data            | Owner                |
| --------------- | -------------------- |
| Application     | Acquisition Platform |
| Offer           | Offer Management     |
| Credit Profile  | Enterprise Credit    |
| Fraud Result    | Enterprise Fraud     |
| Decision Result | Enterprise Decision  |
| Loan Account    | Core Loan System     |

---

## DA-003 Data Duplication

Data duplication is minimized.

Only required references or snapshots are stored.

---

## DA-004 API Based Access

Cross-domain data access occurs through:

* REST APIs
* Events

Never through database queries.

---

## DA-005 Security By Design

All sensitive data requires:

* Encryption
* Access control
* Audit tracking

---

# 3. Logical Data Architecture

```text
+------------------------------------------------+
|            Personal Loan Acquisition            |
+------------------------------------------------+

        |
        |

+-------------------+
| Application Data  |
+-------------------+

        |
        |

+-----------------------------------------------+
|             Service Databases                 |
+-----------------------------------------------+

Application DB

Invitation DB

Offer Acceptance DB

Document DB

Underwriting DB

Tracking DB

Audit DB


        |
        |

+-----------------------------------------------+
|        Enterprise Data Platforms              |
+-----------------------------------------------+

Offer Management

Credit Management

Fraud Platform

Decision Platform

Identity Platform

Funding Platform

```

---

# 4. Data Ownership Model

## Acquisition Platform Owns

### Application Data

Includes:

Application ID

Application Status

Application Lifecycle

Application Dates

Channel Information

---

### Applicant Provided Information

Includes:

Name

Address

Contact Information

Employment Information

Financial Information Provided By Customer

---

### Workflow Data

Includes:

Current State

Pending Activities

Workflow History

Routing Information

---

### Offer Acceptance Data

Includes:

Acceptance Status

Acceptance Timestamp

Consent Records

---

### Underwriting Workflow Data

Includes:

Review Tasks

Conditions

Reviewer Actions

---

### Funding Request Data

Includes:

Funding Request Status

Funding Tracking Reference

---

# 5. Enterprise Owned Data

## Offer Management Platform

Owns:

Offer ID

Offer Terms

Pricing

APR

Campaign Relationship

Invitation

Prospect

---

## Enterprise Credit Management

Owns:

Credit Data

Credit Bureau Information

Credit Attributes

Credit Scores

Credit Models

---

## Enterprise Fraud Platform

Owns:

Fraud Assessment

Fraud Models

Fraud Scores

Fraud Decisions

---

## Enterprise Decision Platform

Owns:

Decision Rules

Risk Models

Eligibility Rules

Approval/Decline Decisions

---

## Identity Platform

Owns:

Identity Verification

Identity Confidence

Identity Risk

---

## Funding Platform

Owns:

Funding Execution

Disbursement Status

---

# 6. Database Architecture

## Pattern

Database Per Service

Example:

```text
application-service

    application-db


invitation-service

    invitation-db


document-service

    document-db


underwriting-service

    underwriting-db
```

---

# 7. Application Database

Owner:

Application Service

## Tables

Application

```text
application_id

invitation_id

offer_id

status

created_date

updated_date

submitted_date

version
```

---

Applicant

```text
applicant_id

application_id

first_name

last_name

dob

email

phone
```

---

Address

```text
address_id

application_id

address_line1

city

state

zip
```

---

Employment

```text
employment_id

application_id

employer

income

employment_status
```

---

# 8. Audit Database

Owner:

Audit Service

Stores:

Business events

Security events

Data changes

Workflow changes

Example:

```text
audit_id

application_id

event_type

old_value

new_value

timestamp

actor

correlation_id
```

---

# 9. Data Classification

## Public

Information allowed for public exposure.

Examples:

Product information

---

## Internal

Business operational data.

Examples:

Application status

---

## Confidential

Customer information.

Examples:

Name

Address

Income

---

## Restricted

Highly sensitive information.

Examples:

SSN

DOB

Financial identifiers

---

# 10. PII Handling

PII must:

* Be encrypted at rest
* Be encrypted in transit
* Have restricted access
* Be masked in logs

---

Example:

Allowed:

```text
Customer first name displayed
```

Not allowed:

```text
Full SSN in logs
```

---

# 11. Encryption Standards

## Data At Rest

Required:

Database encryption

Storage encryption

---

## Data In Transit

Required:

TLS 1.2+

---

## Sensitive Fields

Field-level encryption recommended.

Examples:

SSN

Account numbers

DOB

---

# 12. Data Lifecycle

## Application Retention

Application data:

7 years minimum

(or regulatory requirement)

---

## Audit Retention

Business audit:

7+ years

---

## Temporary Data

Temporary processing data:

Short retention

---

# 13. Data Integration Patterns

## Request/Response

Used for:

Offer retrieval

Verification requests

Decision requests

---

## Event Driven

Used for:

Application state changes

Workflow completion

Notifications

---

# 14. Reporting Architecture

Operational systems shall not support enterprise reporting directly.

Pattern:

```text
Operational Database

        |

CDC / Events

        |

Enterprise Data Platform

        |

Reporting / Analytics
```

---

# 15. Data Consistency Model

## Strong Consistency

Within service boundary.

Example:

Application update.

---

## Eventual Consistency

Across services.

Example:

Decision result updates application status.

---

# 16. Data Validation

Validation occurs at:

API layer

Application service

Database constraints

---

# 17. Data Migration Strategy

Migration approach:

Schema versioning

Backward compatible changes

Zero downtime migration

---

# 18. Data Security Controls

Required:

RBAC

Least privilege

Database encryption

Audit logging

Access reviews

Data masking

---

# 19. Non Functional Requirements

Availability:

99.95%

Backup:

Automated backup

Recovery:

Point-in-time recovery

Performance:

Indexed queries

Caching where required

---

# 20. Related Documents

000-architecture-overview.md

001-logical-architecture.md

002-microservice-boundaries.md

004-api-standards.md

005-event-driven-architecture.md

007-security-architecture.md

008-observability-architecture.md
