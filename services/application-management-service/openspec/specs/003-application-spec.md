# 003 - Application Specification

# Personal Loan Application Management

Version: 1.0

Status: Draft

Capability:

Application Management

---

# 1. Purpose

The Application Management capability manages the personal loan application lifecycle from creation through submission.

The capability provides a unified application experience regardless of how the applicant entered the journey.

Application creation may originate from:

* Invitation Intake
* Direct Intake
* Future acquisition channels

---

# 2. Business Objective

The capability shall:

* Create personal loan applications.
* Maintain application state.
* Manage applicant information.
* Support save and resume.
* Validate application completeness.
* Submit completed applications.
* Track application lifecycle.

---

# 3. Scope

## In Scope

Application creation.

Application update.

Application save.

Application resume.

Applicant information management.

Application validation.

Application submission.

Application status tracking.

Application audit events.

---

## Out Of Scope

Invitation validation.

Offer retrieval.

Offer eligibility.

Customer profile management.

Identity verification execution.

Fraud evaluation.

Credit evaluation.

Decision execution.

Funding execution.

Loan servicing.

---

# 4. Application Lifecycle

The application follows these states:

```text
CREATED

   |

IN_PROGRESS

   |

READY_FOR_SUBMISSION

   |

SUBMITTED

   |

PROCESSING

   |

APPROVED / DECLINED

   |

COMPLETED
```

---

# 5. Application Creation Flow

## Invitation Based Creation

```text
Prospect

 |

Invitation Intake

 |

Application Intake Context

 |

Application Creation

 |

Application Created

 |

Prefilled Data Available

 |

Applicant Completes Application
```

---

## Direct Creation

```text
Prospect

 |

Direct Intake

 |

Application Intake Context

 |

Application Creation

 |

Empty Application Created

 |

Applicant Enters Information
```

---

# 6. Functional Requirements

## FR-001 Create Application

The system shall create a new application.

Input:

Application Intake Context.

Output:

Application Identifier.

Example:

```json
{
 "applicationId":"APP123456",

 "source":"INVITATION",

 "status":"CREATED"
}
```

---

# FR-002 Associate Intake Context

The application shall maintain reference to the intake source.

Example:

```json
{
 "applicationId":"APP123",

 "intakeId":"INT456",

 "applicationSource":"INVITATION"
}
```

---

# FR-003 Prefill Applicant Information

For invitation applications, available information shall be pre-populated.

Possible fields:

* First name
* Last name
* Address
* Phone
* Email

Applicant may modify allowed fields.

---

# FR-004 Maintain Application Data

The system shall support updating application information.

Examples:

* Personal information
* Employment information
* Income information
* Loan request information

---

# FR-005 Save Application Progress

The system shall allow incomplete applications to be saved.

Requirements:

* Persist current state.
* Maintain last updated timestamp.
* Support resume.

---

# FR-006 Resume Application

The system shall allow applicant to continue an existing application.

Input:

Application Identifier.

Output:

Current application state.

---

# FR-007 Validate Application

Before submission the system shall validate required information.

Validation includes:

* Required fields present.
* Data format validation.
* Business rule validation.

---

# FR-008 Submit Application

The system shall submit completed applications for downstream processing.

After submission:

Application becomes immutable except through controlled workflows.

---

# 7. Domain Model

## Aggregate: Application

Represents a personal loan application.

---

## Attributes

```text
applicationId

intakeId

applicationSource

applicationStatus

applicant

loanRequest

createdTimestamp

updatedTimestamp
```

---

# Application Status

```text
CREATED

IN_PROGRESS

READY_FOR_SUBMISSION

SUBMITTED

PROCESSING

APPROVED

DECLINED

CANCELLED

EXPIRED
```

---

# Application Source

```text
INVITATION

DIRECT

PARTNER
```

---

# Applicant Entity

Represents applicant information.

Attributes:

```text
applicantId

firstName

lastName

dateOfBirth

contactInformation

address

employmentInformation
```

---

# Loan Request Entity

Represents requested loan details.

Attributes:

```text
requestedAmount

loanPurpose

term

```

---

# 8. Business Rules

## BR-001

Every application must have an intake source.

---

## BR-002

Application creation does not require an invitation.

---

## BR-003

Invitation information is immutable after application creation.

---

## BR-004

Application status transitions must follow allowed workflow.

---

## BR-005

Submitted applications cannot be modified directly.

---

## BR-006

Sensitive customer information must be protected.

---

# 9. Application Events

## ApplicationCreated

Published when application is created.

Payload:

```json
{
 "applicationId":"APP123",

 "source":"INVITATION",

 "timestamp":"2026-01-01T10:00:00"
}
```

---

## ApplicationSubmitted

Published when application is submitted.

Payload:

```json
{
 "applicationId":"APP123",

 "submittedTimestamp":"2026-01-01T10:30:00"
}
```

---

# 10. Persistence Requirements

Application data shall be persisted.

Required storage:

* Application
* Applicant
* Loan Request
* Application History

Database:

PostgreSQL

Migration:

Flyway

---

# 11. API Requirements

## Create Application

POST

```text
/api/v1/applications
```

---

Request:

```json
{
 "intakeId":"INT123"
}
```

---

Response:

```json
{
 "applicationId":"APP123",

 "status":"CREATED"
}
```

---

# Retrieve Application

GET

```text
/api/v1/applications/{applicationId}
```

---

# Update Application

PUT

```text
/api/v1/applications/{applicationId}
```

---

# Submit Application

POST

```text
/api/v1/applications/{applicationId}/submit
```

---

# 12. Acceptance Criteria

The capability is complete when:

* Application can be created from invitation intake.
* Application can be created without invitation.
* Applicant information can be maintained.
* Application progress can be saved.
* Application can be resumed.
* Complete applications can be submitted.
* Application lifecycle is tracked.
* Audit events are generated.

---

# 13. Related Documents

001-acquisition-business-capabilities.md

002-application-intake-spec.md

04-sequence-diagrams.md

05-api-contracts.md

06-persistence-model.md

07-acceptance-tests.md
