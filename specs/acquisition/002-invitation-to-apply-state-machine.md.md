# acquisition/invitation-to-apply/002-invitation-to-apply-state-machine.md

# Personal Loan Acquisition Platform

## Invitation To Apply State Machine Specification

Version: 1.0

Status: Draft

Owner: Acquisition Domain

---

# 1. Purpose

This document defines the lifecycle states and transitions for the Invitation To Apply (ITA) capability.

The state machine ensures:

* Valid invitation processing
* Controlled state transitions
* Auditability
* Prevention of invalid reuse
* Consistent application creation

---

# 2. Invitation Lifecycle Overview

```text
CREATED

   |

VALIDATING

   |

VALIDATED

   |

OFFER_RETRIEVED

   |

READY_TO_APPLY

   |

APPLICATION_CREATED
```

Failure paths:

```text
VALIDATING

   |

INVALID


OFFER_RETRIEVED

   |

EXPIRED


VALIDATED

   |

REJECTED
```

---

# 3. Invitation States

## 3.1 CREATED

### Description

Invitation exists in the Offer Management platform but has not been accessed by the prospect.

---

### Entry Conditions

* Marketing campaign delivered invitation
* Offer Management created invitation

---

### Allowed Transitions

```text
CREATED
   |
VALIDATING
```

---

# 3.2 VALIDATING

### Description

System is validating invitation details.

---

### Validation Checks

* Invitation ID format
* Invitation existence
* Invitation status
* Access eligibility

---

### Processing

Invitation service calls:

Offer Management API

---

### Allowed Transitions

Success:

```text
VALIDATING
      |
VALIDATED
```

Failure:

```text
VALIDATING
      |
INVALID
```

---

# 3.3 VALIDATED

### Description

Invitation is valid and associated with an eligible offer.

---

### Stored Information

* Invitation ID
* Offer ID
* Validation timestamp
* Correlation ID

---

### Allowed Transitions

```text
VALIDATED

     |

OFFER_RETRIEVED
```

---

# 3.4 OFFER_RETRIEVED

### Description

Offer details successfully retrieved from Offer Management.

---

### Offer Data

Example:

```text
offerId

loanAmount

term

APR

expirationDate
```

---

### Allowed Transitions

Success:

```text
OFFER_RETRIEVED

       |

READY_TO_APPLY
```

Failure:

```text
OFFER_RETRIEVED

       |

EXPIRED
```

---

# 3.5 READY_TO_APPLY

### Description

Invitation is ready for customer application creation.

---

### Customer Experience

Customer sees:

* Offer details
* Prefilled information
* Continue button

---

### Allowed Transitions

```text
READY_TO_APPLY

       |

APPLICATION_CREATED
```

---

# 3.6 APPLICATION_CREATED

### Description

Invitation has been converted into a loan application.

---

### Actions

System creates:

Application

with status:

```text
STARTED
```

---

### Stored Relationship

```text
Invitation ID

        |

Application ID
```

---

### Final State

No further ITA transitions.

---

# 3.7 INVALID

### Description

Invitation validation failed.

---

### Reasons

* Invitation not found
* Invalid format
* Not eligible
* Already invalidated

---

### Final State

No transition.

---

# 3.8 EXPIRED

### Description

Associated offer has expired.

---

### Reasons

* Offer expiration date passed
* Offer withdrawn

---

### Final State

No transition.

---

# 3.9 REJECTED

### Description

Invitation cannot continue.

---

### Reasons

* Business rule violation
* Offer unavailable
* Duplicate usage

---

# 4. State Transition Rules

| Current State   | Event              | Next State          |
| --------------- | ------------------ | ------------------- |
| CREATED         | Validate           | VALIDATING          |
| VALIDATING      | Success            | VALIDATED           |
| VALIDATING      | Failure            | INVALID             |
| VALIDATED       | Retrieve Offer     | OFFER_RETRIEVED     |
| OFFER_RETRIEVED | Valid Offer        | READY_TO_APPLY      |
| OFFER_RETRIEVED | Expired            | EXPIRED             |
| READY_TO_APPLY  | Create Application | APPLICATION_CREATED |

---

# 5. Duplicate Prevention

Rule:

One invitation should create only one application.

---

Validation:

Before application creation:

Check:

```text
invitationId already used?
```

---

If yes:

Return:

```text
ITA-006 Invitation Already Used
```

---

# 6. Timeout Handling

If Offer Management does not respond:

Current State:

```text
VALIDATING
```

Action:

Retry based on integration policy.

---

After retry exhaustion:

Move to:

```text
REJECTED
```

---

# 7. Audit Events

State changes generate events.

---

Example:

```text
InvitationValidated

OfferRetrieved

InvitationExpired

ApplicationCreated
```

---

# 8. Event Model

Example:

```json
{
 "eventType":"InvitationValidated",
 "invitationId":"INV12345",
 "offerId":"OFF987",
 "timestamp":"2026-06-24T10:00:00Z"
}
```

---

# 9. Persistence Model

Invitation table maintains:

```text
invitation_id

offer_id

state

application_id

created_at

updated_at
```

---

# 10. State Transition Ownership

Invitation Service owns:

* Invitation lifecycle
* Validation state
* Conversion tracking

Application Service owns:

* Application lifecycle after creation

---

# 11. Relationship With Application State Machine

ITA creates:

```text
Application STARTED
```

Then Application State Machine manages:

```text
STARTED

SUBMITTED

UNDER_REVIEW

APPROVED

DECLINED

FUNDED
```

---

# 12. Error Mapping

| Condition          | Error   |
| ------------------ | ------- |
| Invitation missing | ITA-001 |
| Offer expired      | ITA-002 |
| Offer inactive     | ITA-003 |
| Offer API failure  | ITA-004 |
| Invalid format     | ITA-005 |
| Already used       | ITA-006 |

---

# 13. State Machine Acceptance Criteria

The system must:

☐ Prevent invalid invitation usage

☐ Prevent duplicate application creation

☐ Track all state changes

☐ Support customer-friendly errors

☐ Maintain audit history

☐ Create application only from valid invitation

---

# Related Documents

* 001-invitation-to-apply-spec.md
* 003-application-spec.md
* 002-application-state-machine.md
* 006-integration-patterns.md
* 003-error-handling-standard.md
