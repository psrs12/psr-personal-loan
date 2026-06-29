# Personal Loan Acquisition Platform

## Component Interaction Flows

Version: 1.0
Status: Current
Owner: Personal Loan Acquisition Platform

---

# 1. Purpose

This document shows how platform components interact during key flows using sequence diagrams. Each diagram identifies the actors, services, external systems, and events involved.

Notation:
- Solid arrow `──►` = synchronous REST call
- Dashed arrow `- - ►` = asynchronous Kafka event
- `[DB]` = database write/read

---

# 2. ITA Flow — Invitation Validation and Application Creation

```
Applicant       UI Shell        invitation-service       application-management-service
    │               │                   │                           │
    │  GET /?token= │                   │                           │
    │──────────────►│                   │                           │
    │               │  POST /validate   │                           │
    │               │──────────────────►│                           │
    │               │  200 prefill data │                           │
    │               │◄──────────────────│                           │
    │               │                   │                           │
    │  (fills form) │                   │                           │
    │──────────────►│                   │                           │
    │               │  POST /applications (submit form data)        │
    │               │───────────────────────────────────────────────►
    │               │                   │         [DB] create application
    │               │                   │         applicationId returned
    │               │◄──────────────────────────────────────────────│
    │  applicationId│                   │                           │
    │◄──────────────│                   │                           │
```

---

# 3. Evaluation Flow — Identity, Fraud, Soft Pull

```
app-management-service     pricing-orchestration-service    Identity Platform    Fraud Platform    Credit Platform
         │                           │                             │                  │                │
         │  PATCH /status PROCESSING │                             │                  │                │
         │──────────────────────────►│                             │                  │                │
         │                           │  POST /identity/verify      │                  │                │
         │                           │────────────────────────────►│                  │                │
         │                           │  POST /fraud/check          │                  │                │
         │                           │───────────────────────────────────────────────►│                │
         │                           │  POST /credit/softpull      │                  │                │
         │                           │────────────────────────────────────────────────────────────────►│
         │                           │                             │                  │                │
         │                           │◄────────────────────────────│ identity result  │                │
         │                           │◄────────────────────────────────────────────── fraud result     │
         │                           │◄──────────────────────────────────────────────────────────────── credit profile
         │                           │                             │                  │                │
         │                           │  GET /offers (with profile)  Offer Platform    │                │
         │                           │────────────────────────────────────────────────────────────────►│ (offer platform)
         │                           │◄─────────────────────────────────────────────────────────────── offers
```

---

# 4. Offer Display, Selection, and Hard Pull Consent Flow

```
Applicant   <pricing-offer-selector>   pricing-orchestration-service    Decision Engine
    │               │                           │                            │
    │  (component   │                           │                            │
    │  embedded in  │  GET /applications/{id}/offers                         │
    │  host page)   │──────────────────────────►│                            │
    │               │                           │──► Offer Management        │
    │               │                           │◄── eligible offers         │
    │               │  200 offers[]             │                            │
    │               │◄──────────────────────────│                            │
    │               │                           │                            │
    │  View offers  │                           │                            │
    │◄──────────────│ (OfferList rendered)       │                            │
    │  Select offer │                           │                            │
    │──────────────►│ (transitions to           │                            │
    │               │  ConsentStep)             │                            │
    │               │                           │                            │
    │  Read consent │                           │                            │
    │  disclosure   │                           │                            │
    │  Confirm      │                           │                            │
    │──────────────►│                           │                            │
    │               │ fires offer-confirmed     │                            │
    │               │ custom event {offerId}    │                            │
    │               │──────────────────────────►│                            │
    │               │                           │  POST /decisions           │
    │               │                           │  (hard pull + offerId)     │
    │               │                           │───────────────────────────►│
    │               │                           │                            │ [hard credit
    │               │                           │                            │  enquiry]
    │               │                           │◄───────────────────────────│ final decision
    │               │                           │                            │
    │               │                           │  [route by outcome]        │
```

---

# 5. Final Decision Routing Flow

```
pricing-orchestration-service     application-management-service     offer-acceptance-service     document-service
           │                                  │                               │                        │
           │  [Decision = APPROVED]           │                               │                        │
           │  PATCH /applications/{id}/status │                               │                        │
           │─────────────────────────────────►│                               │                        │
           │  publish FinalDecisionApproved   │                               │                        │
           │- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - ►│                        │
           │                                  │                               │ [create session]       │
           │                                  │                               │                        │
           │─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─►│
           │  [Decision = DOCUMENTS_REQUIRED] │                               │                        │
           │  publish FinalDecisionDocsRequired                               │                        │
           │                                  │                               │               [ACL map codes]
           │                                  │                               │               [create requirements]
           │                                  │                               │               PATCH /status
           │                                  │◄──────────────────────────────────────────────│
           │                                  │ [transition to DOCUMENTS_REQUIRED]             │
           │                                  │                               │                │
           │  [Decision = DECLINED]           │                               │                │
           │  PATCH /applications/{id}/status │                               │                │
           │─────────────────────────────────►│                               │                │
           │                                  │ [transition to DECLINED]      │                │
```

---

# 6. Applicant Login Flow

```
Applicant       UI Shell       application-management-service
    │               │                      │
    │  Enter:       │                      │
    │  applicationId│                      │
    │  last4SSN     │                      │
    │  dateOfBirth  │                      │
    │──────────────►│                      │
    │               │  POST /applications/login
    │               │─────────────────────►│
    │               │                      │ [find applicant by applicationId]
    │               │                      │ [check status not terminal]
    │               │                      │ [verify: last4SSN + DOB match]
    │               │                      │ [generate JWT session token]
    │               │  200 sessionToken    │
    │               │◄─────────────────────│
    │               │                      │
    │  (store token)│                      │
    │◄──────────────│                      │
    │               │                      │
    │  [portal shell polls status]         │
    │               │  GET /applications/{id}
    │               │─────────────────────►│
    │               │  200 {status: APPROVED}
    │               │◄─────────────────────│
    │               │                      │
    │  [route to MFE based on status]      │
```

---

# 7. Offer Acceptance (E-Sign) Flow

```
Applicant     UI Shell (OfferAcceptanceMfe)    offer-acceptance-service    application-management-service
    │                    │                             │                             │
    │                    │  GET /applications/{id}/declarations                      │
    │                    │────────────────────────────►│                             │
    │                    │  200 declarations list      │                             │
    │                    │◄────────────────────────────│                             │
    │  Review & tick     │                             │                             │
    │  declarations      │                             │                             │
    │───────────────────►│                             │                             │
    │  Submit e-sign     │                             │                             │
    │───────────────────►│  POST /applications/{id}/esign                            │
    │                    │────────────────────────────►│                             │
    │                    │                             │ [validate mandatory decls]  │
    │                    │                             │ [create ESignRecord]        │
    │                    │                             │ [update session → SIGNED]   │
    │                    │                             │                             │
    │                    │                             │  publish ESignCompleted     │
    │                    │                             │- - - - - - - - - - - - - - ►│
    │                    │  200 {eSignId, signedAt}    │   [transition APPROVED      │
    │                    │◄────────────────────────────│    → OFFER_ACCEPTED]        │
    │  Confirmation page │                             │                             │
    │◄───────────────────│                             │                             │
```

---

# 8. Document Collection Flow

```
Applicant  <document-upload-manager>   document-service    Virus Scanner    app-management-service
    │       (document-management-ui)        │                   │                   │
    │               │                       │                   │                   │
    │  (shell embeds component with         │                   │                   │
    │   api-base-url, application-id,       │                   │                   │
    │   session-token attributes)           │                   │                   │
    │               │  GET /requirements    │                   │                   │
    │               │──────────────────────►│                   │                   │
    │               │  200 requirements[]   │                   │                   │
    │               │◄──────────────────────│                   │                   │
    │  View slots   │                       │                   │                   │
    │◄──────────────│ (RequirementCard per  │                   │                   │
    │               │  requirement +        │                   │                   │
    │               │  progress bar)        │                   │                   │
    │  Select file  │                       │                   │                   │
    │  + Upload     │  POST /upload         │                   │                   │
    │──────────────►│──────────────────────►│                   │                   │
    │               │                       │ [store to S3]     │                   │
    │               │                       │ [create record]   │                   │
    │               │                       │ [queue scan]      │                   │
    │               │  201 {documentId}     │──────────────────►│                   │
    │               │◄──────────────────────│                   │                   │
    │               │ [reload requirements] │                   │  [scan complete]  │
    │               │──────────────────────►│                   │                   │
    │               │                       │  POST /internal/virus-scan/result     │
    │               │                       │◄──────────────────│                   │
    │               │                       │ [mark VERIFIED]   │                   │
    │               │                       │ [check all done?] │                   │
    │               │                       │                   │                   │
    │               │                       │  publish DocumentsCompleted           │
    │               │                       │- - - - - - - - - - - - - - - - - - - ►│
    │               │                       │                   │  [transition to   │
    │               │                       │                   │   UNDERWRITING]   │
```

---

# 9. Agent Timeline Query Flow

```
Agent       Agent Tooling    application-management-service    [DB]
  │               │                     │                        │
  │  Search by    │                     │                        │
  │  applicationId│                     │                        │
  │──────────────►│  GET /applications/{id}/timeline            │
  │               │────────────────────►│                        │
  │               │                     │  SELECT events WHERE   │
  │               │                     │  application_id = X    │
  │               │                     │  OR intake_id = X      │
  │               │                     │─────────────────────── ►│
  │               │                     │◄───────────────────────│
  │               │                     │  [sort by occurred_at] │
  │               │  200 timeline       │                        │
  │               │◄────────────────────│                        │
  │  View full    │                     │                        │
  │  history      │                     │                        │
  │◄──────────────│                     │                        │
```

---

# 10. Event Flow Overview

The following diagram shows the complete Kafka event topology:

```
                    pricing-orchestration-service
                              │
                              │ publishes to: pricing-events
                              │
           ┌──────────────────┼────────────────────┐
           │                  │                    │
           ▼                  ▼                    ▼
  offer-acceptance-service  document-service   (future consumers)
           │                  │
           │ ESignCompleted   │ DocumentsCompleted, DocumentUploaded,
           │ offer-acceptance │ DocumentRejected
           │ -events          │ document-events
           │                  │
           └─────┬────────────┘
                 │
                 ▼
    application-management-service
    (consumes both topics, drives state transitions)
```

---

# 11. Anti-Corruption Layer (ACL) — Decision Engine Integration

The Decision Engine uses its own document type vocabulary. The `document-service` ACL translates these codes before they touch the platform domain model:

```
Decision Engine Event                         document-service Domain
──────────────────────────────────────────────────────────────────
FinalDecisionDocumentsRequired {              ACL maps:
  documents: [                   ──────────►  DocumentRequirement {
    { code: "BANK_STMT_3M",                     type: BANK_STATEMENT
      count: 3 },                               count: 3 }
    { code: "PAYSLIP_2",                      DocumentRequirement {
      count: 2 },                               type: PAY_SLIP
    { code: "GOV_ID",                           count: 2 }
      count: 1 }                              DocumentRequirement {
  ]                                             type: GOVERNMENT_ID
}                                               count: 1 }
```

If the Decision Engine sends an unknown code, an `UnknownDocumentTypeException` is thrown, the message goes to the dead-letter topic, and an alert is logged for manual intervention.

---

# 12. Related Documents

| Document | Location |
|----------|----------|
| Functional Flow | `docs/flows/01-functional-flow.md` |
| Scenario Flows | `docs/flows/03-scenario-flows.md` |
| Architecture Overview | `docs/architecture/000-architecture-overview.md` |
| Application State Machine | `docs/architecture/002-application-state-machine.md` |
| Event-Driven Architecture | `docs/architecture/005-event-driven-architecture.md` |
