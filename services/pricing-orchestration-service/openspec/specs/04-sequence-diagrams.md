# Sequence Diagrams

# Pricing Orchestration Service

Version: 1.0

---

# 1. Soft Pull and Offer Pricing

```mermaid
sequenceDiagram
    participant AMS as application-management-service
    participant Kafka
    participant POS as pricing-orchestration-service
    participant CMP as Credit Management Platform
    participant DP as Decision Platform (Pricing Engine)

    AMS ->> Kafka: ApplicationCreatedEvent {applicationId}
    Kafka ->> POS: ApplicationCreatedEventConsumer
    POS ->> CMP: initiateSoftPull(applicationId, applicantReference)
    CMP -->> POS: CreditPullResponse {creditReportReferenceId}
    POS ->> POS: assemblePricingRequest(applicationId, softPullRef)
    POS ->> DP: submitPricingRequest(PricingRequest)
    DP -->> POS: PricingEngineResponse {outcome: OFFERS_GENERATED, offers[]}
    POS ->> Kafka: SoftPullCompleted + offers available
    Note over POS: Offers stored; UI retrieves via pricing-offers-ui
```

---

# 2. Offer Selection and Hard Pull

```mermaid
sequenceDiagram
    actor Applicant
    participant UI as pricing-offers-ui
    participant Kafka
    participant POS as pricing-orchestration-service
    participant CMP as Credit Management Platform

    Applicant ->> UI: Select offer + consent to hard pull
    UI ->> Kafka: ConsentCapturedEvent {applicationId, selectedPricingOfferId}
    Kafka ->> POS: ConsentCapturedEventConsumer
    POS ->> CMP: initiateHardPull(applicationId, applicantReference)
    CMP -->> POS: CreditPullResponse {creditReportReferenceId}
    Kafka ->> POS: HardPullCompleted {hardPullCreditReportReferenceId}
```

---

# 3. Final Decision — Approved

```mermaid
sequenceDiagram
    participant POS as pricing-orchestration-service
    participant DP as Decision Platform
    participant Kafka
    participant OAS as offer-acceptance-service
    participant AMS as application-management-service

    POS ->> DP: submitFinalDecision(applicationId, selectedOfferId, hardPullRef)
    DP -->> POS: FinalDecisionResponse {outcome: APPROVED}
    POS ->> AMS: PATCH /applications/{id}/status → APPROVED
    POS ->> Kafka: FinalDecisionApproved {applicationId, selectedOfferId}
    Kafka ->> OAS: FinalDecisionApprovedConsumer → create OfferAcceptanceSession
```

---

# 4. Final Decision — Declined

```mermaid
sequenceDiagram
    participant POS as pricing-orchestration-service
    participant DP as Decision Platform
    participant Kafka
    participant AMS as application-management-service

    POS ->> DP: submitFinalDecision(...)
    DP -->> POS: FinalDecisionResponse {outcome: DECLINED, reasonCode}
    POS ->> AMS: PATCH /applications/{id}/status → DECLINED
    POS ->> Kafka: FinalDecisionDeclined {applicationId, reasonCode}
```

---

# 5. Final Decision — Documents Required

```mermaid
sequenceDiagram
    participant POS as pricing-orchestration-service
    participant DP as Decision Platform
    participant Kafka
    participant DS as document-service

    POS ->> DP: submitFinalDecision(...)
    DP -->> POS: FinalDecisionResponse {outcome: DOCUMENTS_REQUIRED, documents[decisionEngineCode, count]}
    POS ->> AMS: PATCH /applications/{id}/status → DOCUMENTS_REQUIRED
    POS ->> Kafka: FinalDecisionDocumentsRequired {applicationId, documents[]}
    Note over POS: Document codes forwarded VERBATIM — no mapping in this service
    Kafka ->> DS: FinalDecisionDocumentsRequiredConsumer → apply ACL mapping
```

---

# 6. Final Decision — Referred

```mermaid
sequenceDiagram
    participant POS as pricing-orchestration-service
    participant DP as Decision Platform
    participant Kafka
    participant AMS as application-management-service

    POS ->> DP: submitFinalDecision(...)
    DP -->> POS: FinalDecisionResponse {outcome: REFERRED}
    POS ->> AMS: PATCH /applications/{id}/status → REFERRED
    POS ->> Kafka: FinalDecisionReferred {applicationId}
    Note over POS: Selected offer retained unchanged. No new hard pull.
```
