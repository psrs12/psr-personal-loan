# 002 - Invitation To Apply Specification

## Personal Loan Application Intake

Version: 1.0

Status: Draft

Capability:

Application Management

Sub Capability:

Application Intake

Channel:

Invitation To Apply (ITA)

---

# 1. Purpose

The Invitation To Apply (ITA) intake capability enables a prospect who received a marketing invitation to initiate a Personal Loan application.

The capability validates the invitation, retrieves associated offer information, retrieves available customer information, and initializes the application journey.

ITA is an application entry channel.

It does not own:

* Application lifecycle
* Offer lifecycle
* Customer master data
* Credit decisioning
* Funding

---

# 2. Business Objective

The capability shall:

* Provide a simplified application start experience.
* Validate invitation eligibility.
* Retrieve pre-approved offer information.
* Reduce applicant data entry.
* Pre-populate available application information.
* Create an application intake context.
* Enable Application Creation.

---

# 3. Scope

## In Scope

Invitation validation.

Offer retrieval.

Customer reference retrieval.

Customer information retrieval.

Application intake context creation.

Prefill data preparation.

Audit event generation.

---

## Out Of Scope

Campaign creation.

Marketing eligibility.

Offer generation.

Offer pricing.

Customer profile maintenance.

Application completion.

Application submission.

Credit decision.

Loan funding.

---

# 4. Business Actors

## Prospect

A person who received a personal loan invitation and starts an application.

---

## Application Management

Responsible for:

* Receiving intake request.
* Creating intake context.
* Starting application journey.

---

## External Systems

### Offer Management Platform

Provides:

* Invitation validation.
* Offer details.
* Offer identifier.
* Customer reference identifier.

---

### Customer Profile Platform

Provides:

* Applicant information.
* Existing customer details.

---

# 5. Business Flow

## Invitation Based Application Start

```text
Prospect

   |

Enter Invitation Identifier

   |

Application Intake

   |

Validate Invitation

   |

Retrieve Offer

   |

Retrieve Customer Reference

   |

Retrieve Customer Information

   |

Create Intake Context

   |

Create Application

   |

Prefill Application Data

   |

Continue Application Journey
```

---

# 6. Functional Requirements

## FR-001 Validate Invitation

The system shall validate the invitation identifier.

Validation includes:

* Identifier exists.
* Invitation is active.
* Invitation is not expired.
* Invitation can be used.

---

## FR-002 Retrieve Offer

The system shall retrieve the offer associated with the invitation.

Offer information includes:

* Offer identifier.
* Loan amount.
* Term.
* APR.
* Expiration date.

---

## FR-003 Retrieve Customer Reference

The system shall retrieve the customer reference identifier from the offer.

Customer reference is used only for customer lookup.

---

## FR-004 Retrieve Customer Information

The system shall retrieve available customer information.

Possible information:

* First name.
* Last name.
* Address.


---

## FR-005 Create Intake Context

The system shall create an application intake context.

Example:

```json
{
 "intakeId":"INT123",

 "source":"INVITATION",

 "invitationId":"ITA123",

 "offerId":"OFF456",

 "customerReferenceId":"CUST789"
}
```

---

# 7. Business Rules

## BR-001

Invitation must exist.

---

## BR-002

Invitation must be active.

---

## BR-003

Expired invitations cannot start an application.

---

## BR-004

Offer must be available.

---

## BR-005

Customer reference must be present when customer lookup is required.

---

## BR-006

Application creation must not depend on invitation availability.

Direct applications must follow a separate intake path.

---

# 8. Domain Model

## ApplicationIntakeContext

Represents the origin and initialization state of an application journey.

Attributes:

```
intakeId

applicationSource

invitationId

offerId

customerReferenceId

status

createdTimestamp
```

---

## Application Source

Values:

```
INVITATION

DIRECT

PARTNER
```

---

# 9. Status Model

## Intake Status

```
* VALIDATED
* COMPLETED
* FAILED
* EXPIRED
```

---

# 10. Integration Requirements

## Offer Management Integration

Pattern:

Synchronous API

Required Operations:

```
validateInvitation()

retrieveOffer()
```

---

## Customer Profile Integration

Pattern:

Synchronous API

Required Operation:

```
retrieveCustomer(customerReferenceId)
```

---

# 11. Error Handling

## Invalid Invitation

Error:

```
INVITATION_NOT_FOUND
```

---

## Expired Invitation

Error:

```
INVITATION_EXPIRED
```

---

## Offer Failure

Error:

```
OFFER_UNAVAILABLE
```

---

## Customer Lookup Failure

Error:

```
CUSTOMER_INFORMATION_UNAVAILABLE
```

---

# 12. Acceptance Criteria

The capability is complete when:

* Valid invitation creates intake context.
* Offer information is retrieved.
* Customer information is retrieved.
* Prefill data is available.
* Application creation can start.
* Invalid invitations are rejected.
* External failures are handled gracefully.

---

# 13. Related Documents

001-acquisition-business-capabilities.md

003-application-spec.md

04-sequence-diagrams.md

05-api-contracts.md

06-persistence-model.md

07-acceptance-tests.md
