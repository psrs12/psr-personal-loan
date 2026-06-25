# design/002-database-design-guidelines.md

# Personal Loan Acquisition Platform

## Database Design Guidelines

Version: 1.0

Status: Draft

Owner: Application Architecture

---

# 1. Purpose

This document defines database design standards for all services in the Personal Loan Acquisition Platform.

The objectives are:

* Consistent database design
* Performance optimization
* Data integrity
* Security compliance
* Maintainability

---

# 2. Database Principles

## DB-001 Database Ownership

Each service owns its database.

Example:

```text
Application Service

owns

application_db
```

No other service accesses this database directly.

---

## DB-002 Schema Ownership

A service owns:

* Tables
* Indexes
* Constraints
* Migrations
* Stored procedures (if required)

---

## DB-003 API Over Database

Services communicate using:

* APIs
* Events

Never:

* Direct SQL access
* Shared tables

---

# 3. Database Architecture

Pattern:

Database per service

```text
application-service

        |

 application_db


invitation-service

        |

 invitation_db
```

---

# 4. Database Technology

Preferred:

Relational database

Examples:

PostgreSQL

Oracle

Enterprise approved database

---

# 5. Schema Design Standards

## Normalization

Default:

Third normal form

Exceptions allowed for:

Performance

Reporting models

---

# 6. Table Naming Standards

Use:

snake_case

Example:

Correct:

```sql
loan_application
```

Incorrect:

```sql
LoanApplication
```

---

# 7. Primary Key Standards

Every table requires:

Primary key

Preferred:

UUID

Example:

```sql
application_id UUID PRIMARY KEY
```

---

# 8. Identifier Standards

IDs should be:

Unique

Non-sequential externally

Non-sensitive

---

Example:

Good:

```text
APP-8F73A91
```

Avoid:

```text
123456
```

---

# 9. Common Audit Columns

Every business table requires:

```sql
created_at

created_by

updated_at

updated_by

version
```

---

Example:

```sql
created_at TIMESTAMP

updated_at TIMESTAMP
```

---

# 10. Soft Delete

Preferred approach:

Soft delete

Column:

```sql
deleted_flag
```

or

```sql
deleted_at
```

---

Use when:

Regulatory retention required

Historical tracking required

---

# 11. Entity Versioning

Optimistic locking required.

Example:

```sql
version INTEGER
```

---

Purpose:

Prevent lost updates.

---

# 12. Application Data Model Example

## application

```text
application_id

invitation_id

offer_id

status

created_at

updated_at
```

---

## applicant

```text
applicant_id

application_id

first_name

last_name

email

phone
```

---

# 13. Foreign Keys

Use foreign keys within service boundary.

Example:

```sql
application_id
```

between:

application

applicant

---

Avoid cross-service foreign keys.

---

# 14. Indexing Standards

Indexes required for:

Primary keys

Foreign keys

Search fields

Frequently queried fields

---

Example:

```sql
CREATE INDEX idx_application_status

ON application(status);
```

---

# 15. Query Performance

Avoid:

Full table scans

Unbounded queries

Large joins

---

Use:

Pagination

Filtering

Projection queries

---

# 16. Pagination Standards

Never return unlimited records.

Example:

```text
page=0

size=50
```

---

# 17. Data Access Layer

Use:

Repository pattern

Example:

```java
ApplicationRepository
```

---

Repository should not contain:

Business rules

External calls

---

# 18. ORM Standards

Preferred:

JPA/Hibernate

---

Rules:

Avoid entity leakage

Avoid lazy loading surprises

Control fetch strategy

---

# 19. Entity Mapping Guidelines

Avoid:

Bidirectional relationships everywhere

Example:

Avoid:

```java
Application -> Applicant

Applicant -> Application
```

unless required.

---

# 20. Fetch Strategy

Default:

LAZY

Use EAGER only when justified.

---

# 21. Transaction Management

Transactions managed by:

Application service layer

Example:

```java
@Transactional
submitApplication()
```

---

# 22. Migration Standards

All schema changes require:

Version-controlled migrations

Example:

```text
V1_create_application.sql

V2_add_status_column.sql
```

---

# 23. Migration Rules

Never:

Modify old migration files

Drop production tables directly

---

Always:

Create new migration

Test rollback

---

# 24. Data Integrity

Use:

Constraints

Validation

Unique indexes

Example:

```sql
UNIQUE(invitation_id)
```

---

# 25. Sensitive Data Handling

Sensitive data must have:

Encryption

Masking

Access control

---

Examples:

SSN

DOB

Bank account

---

# 26. Data Masking

Production logs:

Never expose:

Full PII

Example:

```text
XXX-XX-1234
```

---

# 27. Backup Requirements

Database backup:

Automated

Encrypted

Tested

---

# 28. Performance Optimization

Use:

Indexes

Caching

Query optimization

Connection pooling

---

# 29. Connection Management

Configure:

Maximum pool size

Timeout

Idle timeout

---

# 30. Database Monitoring

Monitor:

Connection usage

Query latency

Locks

Deadlocks

Storage

---

# 31. Disaster Recovery

Support:

Replication

Backup restore

Failover

---

# 32. Data Retention

Retention must follow:

Business requirement

Regulatory requirement

---

# 33. Reporting Data

Operational databases should not support analytics directly.

Use:

CDC

Events

Data platform

---

# 34. Testing Requirements

Database testing:

Migration testing

Integration testing

Performance testing

Backup recovery testing

---

# 35. Database Checklist

Before production:

☐ Schema reviewed

☐ Indexes created

☐ Migration tested

☐ Backup configured

☐ Security validated

☐ Performance tested

---

# Related Documents

001-service-design-guidelines.md

003-error-handling-standard.md

004-logging-standard.md

005-testing-strategy.md

006-coding-standard.md
