# Sequence Diagrams

# Offer Acceptance Service

Version: 1.0

---

# 1. Session Creation on Approval

```mermaid
sequenceDiagram
    participant Pricing as pricing-orchestration-service
    participant Kafka
    participant OAS as offer-acceptance-service
    participant DB as offer_acceptance DB

    Pricing ->> Kafka: FinalDecisionApproved {applicationId}
    Kafka ->> OAS: FinalDecisionApprovedConsumer
    OAS ->> DB: findSessionByApplicationId
    DB -->> OAS: empty (no session yet)
    OAS ->> OAS: OfferAcceptanceSession.create(applicationId)
    OAS ->> DB: save(session)
    DB -->> OAS: saved
```

---

# 2. Session Creation — Idempotency (Duplicate Event)

```mermaid
sequenceDiagram
    participant Kafka
    participant OAS as offer-acceptance-service
    participant DB as offer_acceptance DB

    Kafka ->> OAS: FinalDecisionApproved (duplicate)
    OAS ->> DB: findSessionByApplicationId
    DB -->> OAS: session exists (PENDING or SIGNED)
    OAS ->> OAS: discard — log duplicate
```

---

# 3. Applicant Retrieves Declarations

```mermaid
sequenceDiagram
    actor Applicant
    participant UI as application-management-ui
    participant OAS as offer-acceptance-service
    participant DB as offer_acceptance DB

    Applicant ->> UI: View offer acceptance page
    UI ->> OAS: GET /applications/{id}/declarations
    OAS ->> DB: findSessionByApplicationId
    DB -->> OAS: OfferAcceptanceSession (status: PENDING)
    OAS -->> UI: List<DeclarationResponse>
    UI -->> Applicant: Display declarations
```

---

# 4. Applicant Submits E-Sign — All Mandatory Accepted

```mermaid
sequenceDiagram
    actor Applicant
    participant UI as application-management-ui
    participant OAS as offer-acceptance-service
    participant DB as offer_acceptance DB
    participant Kafka
    participant AMS as application-management-service

    Applicant ->> UI: Accept all declarations and sign
    UI ->> OAS: POST /applications/{id}/esign {acceptedDeclarationIds, ipAddress}
    OAS ->> DB: findSessionByApplicationId
    DB -->> OAS: OfferAcceptanceSession (status: PENDING)
    OAS ->> OAS: session.sign(acceptedDeclarationIds, ipAddress)
    OAS ->> DB: save(session, eSignRecord)
    DB -->> OAS: saved
    OAS ->> Kafka: ESignCompleted {applicationId, signedAt, correlationId}
    OAS -->> UI: 200 OK {eSignId, signedAt}
    Kafka ->> AMS: ESignCompleted consumer
    AMS ->> AMS: transition to OFFER_ACCEPTED
```

---

# 5. E-Sign Rejected — Missing Mandatory Declaration

```mermaid
sequenceDiagram
    actor Applicant
    participant UI
    participant OAS as offer-acceptance-service

    Applicant ->> UI: Submit without accepting all mandatory declarations
    UI ->> OAS: POST /applications/{id}/esign {incomplete acceptedDeclarationIds}
    OAS ->> OAS: session.sign() → MandatoryDeclarationMissingException
    OAS -->> UI: 422 MANDATORY_DECLARATION_NOT_ACCEPTED
    UI -->> Applicant: Display validation error
```

---

# 6. E-Sign Rejected — Already Signed

```mermaid
sequenceDiagram
    actor Applicant
    participant UI
    participant OAS as offer-acceptance-service
    participant DB as offer_acceptance DB

    Applicant ->> UI: Re-submit e-sign
    UI ->> OAS: POST /applications/{id}/esign
    OAS ->> DB: findSessionByApplicationId
    DB -->> OAS: OfferAcceptanceSession (status: SIGNED)
    OAS ->> OAS: session.sign() → AlreadySignedException
    OAS -->> UI: 409 ESIGN_ALREADY_COMPLETED
```
