Personal Loan Acquisition Platform
Coding Standards

Version: 1.0

Status: Draft

Owner: Application Architecture

# 1. Purpose

This document defines coding standards for all services within the Personal Loan Acquisition Platform.

Objectives:

Consistent code structure
Maintainable codebase
High readability
Improved testability
Reduced technical debt
Faster onboarding
# 2. Technology Standards
Programming Language
Java 21
Framework
Spring Boot 3.x
Build Tool
Maven

Standardized parent POM required.

Required Libraries
Spring Web
Spring Validation
Spring Security
Spring Data JPA
Spring Actuator
OpenAPI
Micrometer
Resilience4j
MapStruct
JUnit 5
Mockito
Testcontainers

# 3. General Coding Principles
CODE-001 Readability First

Code should be easy to read before being clever.

CODE-002 Single Responsibility

Each class should have one primary responsibility.

CODE-003 Prefer Composition

Prefer composition over inheritance.

CODE-004 Business Intent

Code should express business meaning.

Bad:

status = 4;

Good:

status = ApplicationStatus.SUBMITTED;
# 4. Package Structure

Standard service layout:

com.company.loan.application

├── api
├── application
├── domain
├── infrastructure
└── common
# 5. Naming Conventions
Classes

Use PascalCase.

Examples:

ApplicationService
InvitationController
FraudDecisionAdapter
Methods

Use camelCase.

Examples:

createApplication()
submitApplication()
validateInvitation()
Variables

Use meaningful names.

Good:

applicationId
creditDecision
offerExpirationDate

Bad:

x
data
temp
# 6. Constants

Use:

public static final

Naming:

MAX_RETRY_COUNT
DEFAULT_TIMEOUT_SECONDS

Avoid magic numbers.

Bad:

if(retryCount > 3)

Good:

if(retryCount > MAX_RETRY_COUNT)
# 7. Controller Standards

Controllers should:

Validate requests
Invoke application services
Return responses

Controllers should NOT:

Contain business logic
Access repositories
Call external systems

Good:

@PostMapping
public ResponseEntity<ApplicationResponse> create(
    @Valid @RequestBody CreateApplicationRequest request) {

    return ResponseEntity.ok(
        applicationService.create(request));
}
# 8. Service Standards

Application services orchestrate workflows.

Responsibilities:

Transactions
Business orchestration
Domain interactions
External integrations

Avoid God classes.

Maximum recommended:

500 lines

Split large services.

# 9. Domain Model Standards

Domain objects contain behavior.

Bad:

application.setStatus(APPROVED);

Good:

application.approve();
# 10. Entity Standards

Entities:

Must have identity
Must protect invariants
Should contain behavior

Example:

Application
Invitation
Applicant
# 11. DTO Standards

Separate API contracts from domain models.

Never expose entities directly.

Example:

CreateApplicationRequest

ApplicationResponse
# 12. MapStruct Standards

Preferred mapper framework:

MapStruct

Avoid manual mapping where possible.

Example:

@Mapper
public interface ApplicationMapper {
}
# 13. Lombok Standards

Allowed:

@Getter
@Setter
@Builder
@RequiredArgsConstructor

Avoid excessive usage.

Do not hide important logic.

# 14. Null Handling

Prefer:

Optional<T>

for return values.

Never:

return null;

when Optional is appropriate.

# 15. Exception Standards

Use domain-specific exceptions.

Good:

InvitationExpiredException

Bad:

Exception
RuntimeException
# 16. Logging Standards

Use:

@Slf4j

Always include:

correlationId
applicationId

when available.

Never log:

SSN
DOB
Password
Token
# 17. Validation Standards

Use Bean Validation.

Example:

@NotNull
@NotBlank
@Size(max = 100)

Avoid manual validation.

# 18. API Standards

Follow OpenAPI-first design.

Every endpoint requires:

Request schema
Response schema
Error schema
Examples
# 19. Database Access Standards

Use repositories.

Never:

EntityManager

directly in business code unless justified.

Repository names:

ApplicationRepository
InvitationRepository
# 20. Transaction Standards

Transactions belong in application services.

Example:

@Transactional
public void submitApplication()

Avoid nested transactions.

# 21. Event Standards

Event names use past tense.

Good:

ApplicationCreated
ApplicationSubmitted
OfferAccepted

Bad:

CreateApplication
SubmitApplication
# 22. REST Endpoint Standards

Resource-oriented URLs.

Good:

POST /applications

GET /applications/{applicationId}

Bad:

POST /createApplication

GET /getApplication
# 23. API Versioning

Standard:

/api/v1/applications

Breaking changes require new version.

# 24. Test Standards

Naming:

shouldCreateApplication()

shouldRejectExpiredInvitation()

Avoid generic names.

Bad:

test1()
# 25. Code Review Standards

Every PR must verify:

☐ Business rules correct

☐ Security reviewed

☐ Tests included

☐ Logging added

☐ Error handling implemented

☐ API documentation updated

☐ No duplicated code

# 26. SonarQube Standards

Quality gate:

Coverage >= 80%

No blocker issues

No critical vulnerabilities

No duplicated code > 3%
# 27. Security Coding Rules

Never:

Hardcode secrets
Store passwords
Disable security checks

Use:

Secrets manager
Environment variables
Secure vault integration
# 28. Performance Standards

Avoid:

N+1 queries
Large object graphs
Unbounded collections

Use:

Pagination
Caching
Projection queries
# 29. Documentation Standards

Public classes require JavaDoc when business behavior is non-obvious.

Complex business rules require:

Documentation
Decision records
Examples
# 30. Definition of Done

Code is complete when:

☐ Compiles successfully

☐ Unit tests pass

☐ Integration tests pass

☐ Contract tests pass

☐ Security scans pass

☐ SonarQube passes

☐ Documentation updated

☐ Code review approved

☐ Deployable to production

Related Documents
001-service-design-guidelines.md
002-database-design-guidelines.md
003-error-handling-standard.md
004-logging-standard.md
005-testing-strategy.md
004-api-standards.md
007-security-architecture.md
010-non-functional-requirements.md