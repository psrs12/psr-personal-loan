# acquisition/invitation-to-apply/001-invitation-to-apply-spec.md

# Personal Loan Acquisition Platform

## Invitation To Apply (ITA) Functional Specification

Version: 1.0

Status: Draft

Owner: Acquisition Domain

---

# 1. Purpose

The Invitation To Apply (ITA) capability allows a prospect who received a marketing invitation to initiate a personal loan application.

The capability validates the invitation identifier, retrieves the associated offer from the Enterprise Offer Management Platform, and pre-populates applicant information to simplify the application experience.

Campaign Management and Offer Management are external enterprise capabilities and are out of scope for this application.

---

# 2. Business Objective

Provide a streamlined application entry point for pre-screened prospects by:

* Validating invitation identifiers
* Retrieving eligible loan offers
* Pre-filling applicant information
* Creating a new loan application
* Reducing application abandonment
* Improving customer experience

---

# 3. Scope

## In Scope

* Invitation ID validation
* Offer retrieval
* Offer eligibility verification
* Offer expiration validation
* Applicant information prefill
* Application creation
* Audit tracking
* Invitation acceptance tracking

---

## Out Of Scope

* Campaign creation
* Campaign management
* Offer generation
* Offer pricing
* Offer strategy
* Credit decisioning
* Fraud decisioning
* Underwriting
* Funding

---

# 4. Business Process

```text
Prospect

   |

Enter Invitation ID

   |

Validate Invitation

   |

Retrieve Offer

   |

Validate Offer

   |

Display Offer

   |

Pre-Fill Applicant Information

   |

Create Application

   |

Continue Application Journey
```

---

# 5. Business Actors

## Primary Actor

Prospect

A consumer who received an invitation to apply.

---

## Supporting Systems

Offer Management Platform

Provides:

* Offer details
* Applicant profile
* Offer status
* Expiration information

---

Application Service

Provides:

* Application creation
* Application persistence

---

Audit Service

Provides:

* User activity tracking
* Compliance records

---

# 6. Business Capabilities

## ITA-001 Validate Invitation

Validate invitation identifier submitted by prospect.

---

## ITA-002 Retrieve Offer

Retrieve offer associated with invitation.

---

## ITA-003 Validate Offer

Validate:

* Offer active
* Offer eligible
* Offer not expired

---

## ITA-004 Prefill Applicant Data

Populate application using:

* First Name
* Last Name
* Address

---

## ITA-005 Create Application

Create new application instance.

---

## ITA-006 Audit Activity

Record invitation usage.

---

# 7. Business Rules

## BR-001 Invitation Required

Invitation ID must be provided.

---

## BR-002 Invitation Must Exist

Invitation must be associated with a valid offer.

---

## BR-003 Offer Must Be Active

Inactive offers cannot be accepted.

---

## BR-004 Offer Must Not Be Expired

Expired offers cannot be used.

---

## BR-005 One Application Per Invitation

A used invitation cannot create multiple applications unless explicitly allowed by business configuration.

---

## BR-006 Applicant Data Source

Applicant prefill data is sourced from Offer Management.

---

## BR-007 Offer Is System Of Record

Offer Management Platform remains the system of record for offer information.

---

# 8. Functional Requirements

## FR-001 Enter Invitation ID

System shall allow prospect to enter invitation identifier.

Example:

```text
INV-123456789
```

---

## FR-002 Validate Invitation

System shall validate invitation format.

---

## FR-003 Retrieve Offer

System shall invoke Offer Management API.

Request:

```text
GET /offers/invitations/{invitationId}
```

---

## FR-004 Validate Offer Status

System shall verify:

* Active
* Available
* Not expired

---

## FR-005 Display Offer Summary

System shall display:

* Offer Amount Range
* APR Range
* Expiration Date

Values displayed are determined by business requirements.

---

## FR-006 Prefill Applicant Information

System shall prefill:

* First Name
* Last Name
* Address Line 1
* Address Line 2
* City
* State
* Postal Code

---

## FR-007 Create Application

System shall create application in STARTED status.

---

## FR-008 Associate Invitation

System shall associate:

* Invitation ID
* Offer ID

with the application.

---

## FR-009 Continue Journey

System shall redirect prospect to application flow.

---

# 9. Alternate Flows

## AF-001 Invalid Invitation

If invitation cannot be found:

System shall display:

"Invitation could not be validated."

---

## AF-002 Expired Offer

If offer is expired:

System shall display:

"This offer is no longer available."

---

## AF-003 Inactive Offer

If offer is inactive:

System shall prevent application creation.

---

## AF-004 Offer Service Unavailable

If Offer Management is unavailable:

System shall display generic error message.

System shall log technical details.

---

# 10. Application State Impact

Successful ITA creates:

```text
STARTED
```

application state.

---

State transition:

```text
NONE

  |

STARTED
```

---

# 11. External Dependencies

## Enterprise Offer Management Platform

Ownership:

Enterprise Offer Management

Responsibilities:

* Offer retrieval
* Offer validation
* Prospect profile retrieval

Protocol:

REST API

---

# 12. Data Elements

## Invitation Information

```text
invitationId
offerId
offerExpirationDate
offerStatus
```

---

## Applicant Information

```text
firstName
lastName
addressLine1
addressLine2
city
state
postalCode
```

---

## Application Information

```text
applicationId
applicationStatus
createdDate
```

---

# 13. Audit Requirements

Capture:

```text
eventType
invitationId
offerId
applicationId
timestamp
correlationId
```

---

Example Event:

```text
InvitationValidated
```

---

# 14. Security Requirements

* Invitation ID validation required
* HTTPS required
* Audit logging required
* PII masking in logs required

---

# 15. Error Codes

| Code    | Description               |
| ------- | ------------------------- |
| ITA-001 | Invitation Not Found      |
| ITA-002 | Offer Expired             |
| ITA-003 | Offer Inactive            |
| ITA-004 | Offer Service Unavailable |
| ITA-005 | Invalid Invitation Format |

---

# 16. Success Criteria

A prospect can:

1. Enter invitation ID
2. Validate invitation
3. Retrieve associated offer
4. View offer details
5. Receive prefilled applicant information
6. Create application
7. Continue application journey

without manual re-entry of known applicant information.

---

# Related Documents

* 001-acquisition-business-capabilities.md
* 002-application-state-machine.md
* 003-application-spec.md
* 004-api-standards.md
* 006-integration-patterns.md
* Invitation Service OpenAPI Specification
* Application Service OpenAPI Specification
