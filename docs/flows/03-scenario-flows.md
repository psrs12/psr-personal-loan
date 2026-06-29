# Personal Loan Acquisition Platform

## Scenario Flow Diagrams

Version: 1.0
Status: Current
Owner: Personal Loan Acquisition Platform

---

# 1. Purpose

This document describes end-to-end scenario walkthroughs for the most important applicant journeys. Each scenario is presented as a step-by-step flow with the state machine transitions and events that occur at each step.

---

# 2. Scenario Index

| # | Scenario | Decision Outcome | Path |
|---|----------|-----------------|------|
| S1 | ITA Happy Path — Approved, Signed | APPROVED | Invitation → Application → Decision → E-Sign → Funded |
| S2 | Documents Required — Upload and Complete | DOCUMENTS_REQUIRED | Invitation → Application → Decision → Upload → Underwriting |
| S3 | Declined Application | DECLINED | Invitation → Application → Decision → Adverse Action |
| S4 | Direct Application (No Invitation) | APPROVED | Direct → Application → Decision → E-Sign → Funded |
| S5 | Applicant Login and Status Check | N/A | Login → Portal Shell → MFE Routing |
| S6 | Document Upload — Rejected File | DOCUMENTS_REQUIRED | Upload → Virus Fail → Re-upload |
| S7 | Already Signed (Idempotency Check) | APPROVED | Duplicate E-Sign Attempt |

---

# 3. Scenario S1 — ITA Happy Path: APPROVED → Funded

**Description:** Customer receives an invitation, completes the application, is approved, signs the offer, and receives funding.

```
Step  Actor                  Action / Event                           App Status
────  ─────────────────────  ───────────────────────────────────────  ──────────────
 1    Bank                   Generate invitation token for customer   —
 2    Customer               Click invitation link                    —
 3    invitation-service     Validate token; return prefill data      —
 4    Customer               Review form; click Submit                —
 5    application-mgmt-svc   Create Application                       CREATED
 6    application-mgmt-svc   Applicant begins journey                 STARTED
 7    application-mgmt-svc   Data entered and saved                   IN_PROGRESS
 8    Customer               Submit final form                        —
 9    application-mgmt-svc   Receive submission                       SUBMITTED
10    pricing-orch-svc       Run identity, fraud, soft pull           PROCESSING
11    pricing-orch-svc       Retrieve offers from Offer Platform       PROCESSING
12    Customer               View offers in <pricing-offer-selector>   PROCESSING
      pricing-offers-ui      OfferList rendered; customer selects      PROCESSING
      pricing-offers-ui      ConsentStep rendered; customer confirms   PROCESSING
      pricing-offers-ui      Fires offer-confirmed custom event        PROCESSING
13    Decision Engine        Execute hard pull; return APPROVED        —
14    pricing-orch-svc       PATCH /status → APPROVED                 APPROVED
15    pricing-orch-svc       Publish FinalDecisionApproved (Kafka)    APPROVED
16    offer-acceptance-svc   Consume event; create OfferAcceptanceSession  APPROVED
17    Customer               Log in to portal (applicationId+SSN+DOB) APPROVED
18    application-mgmt-svc   Issue JWT session token                  APPROVED
19    UI Shell               Poll status; route to OfferAcceptanceMfe APPROVED
20    Customer               Read declarations; tick all mandatory     APPROVED
21    Customer               Click "Sign and accept offer"             APPROVED
22    offer-acceptance-svc   Validate; create ESignRecord              APPROVED
23    offer-acceptance-svc   Publish ESignCompleted (Kafka)           APPROVED
24    application-mgmt-svc   Consume ESignCompleted                   OFFER_ACCEPTED
25    UI Shell               Poll detects OFFER_ACCEPTED; show Confirmation  OFFER_ACCEPTED
26    funding-request-svc    Submit funding request                    FUNDING_PENDING
27    Funding Platform       Disburse funds                            FUNDED
28    application-mgmt-svc   Transition to COMPLETED                  COMPLETED
```

**State transitions:**
```
CREATED → STARTED → IN_PROGRESS → SUBMITTED → PROCESSING → APPROVED → OFFER_ACCEPTED → FUNDING_PENDING → FUNDED → COMPLETED
```

---

# 4. Scenario S2 — Documents Required: Upload and Complete

**Description:** Customer is conditionally approved pending document submission. All documents are uploaded cleanly and the application moves to underwriting.

```
Step  Actor                  Action / Event                           App Status
────  ─────────────────────  ───────────────────────────────────────  ──────────────
 1–12  (same as S1 steps 1–12 — invitation through offer selection)   PROCESSING
13    Decision Engine        Execute hard pull; return DOCUMENTS_REQUIRED  —
14    pricing-orch-svc       PATCH /status → APPROVED (interim)           APPROVED
15    pricing-orch-svc       Publish FinalDecisionDocumentsRequired (Kafka)  APPROVED
16    document-service       Consume event                                APPROVED
17    document-service       ACL: map codes → DocumentRequirements        APPROVED
18    document-service       Persist requirements                          APPROVED
19    document-service       PATCH /status → DOCUMENTS_REQUIRED            DOCUMENTS_REQUIRED
20    Customer               Log in to portal                              DOCUMENTS_REQUIRED
21    UI Shell               Poll status; embed <document-upload-manager>  DOCUMENTS_REQUIRED
22    document-management-ui GET /requirements → render 3 RequirementCards DOCUMENTS_REQUIRED
23    Customer               Upload bank statement (file 1 of 3)           DOCUMENTS_REQUIRED
24    document-service       Store to S3; create DocumentRecord (UPLOADED) DOCUMENTS_REQUIRED
25    Virus Scanner          Scan complete; result: CLEAN                  DOCUMENTS_REQUIRED
26    document-service       POST /internal/virus-scan/result              DOCUMENTS_REQUIRED
27    document-service       Mark record VERIFIED; requirement count: 1/3  DOCUMENTS_REQUIRED
28    Customer               Upload bank statements (files 2, 3 of 3)      DOCUMENTS_REQUIRED
29    Virus Scanner          Both scans: CLEAN                             DOCUMENTS_REQUIRED
30    document-service       Mark records VERIFIED; requirement COMPLETED  DOCUMENTS_REQUIRED
31    Customer               Upload payslips (2 of 2)                      DOCUMENTS_REQUIRED
32    document-service       Both VERIFIED; payslip requirement COMPLETED  DOCUMENTS_REQUIRED
33    document-service       All requirements COMPLETED                    DOCUMENTS_REQUIRED
34    document-service       Publish DocumentsCompleted (Kafka)            DOCUMENTS_REQUIRED
35    application-mgmt-svc   Consume DocumentsCompleted                    UNDERWRITING
36    Underwriting Team      Review documents; issue approval              UNDERWRITING
37    application-mgmt-svc   PATCH /status → APPROVED                      APPROVED
38    (continue from S1 step 17 — portal login → e-sign → funding)         ...
```

**State transitions:**
```
PROCESSING → APPROVED* → DOCUMENTS_REQUIRED → UNDERWRITING → APPROVED → OFFER_ACCEPTED → FUNDED → COMPLETED
*Note: pricing-orchestration-service first sets APPROVED before document-service overrides to DOCUMENTS_REQUIRED
```

---

# 5. Scenario S3 — Declined Application

**Description:** Customer's application is declined by the Decision Engine after the hard pull.

```
Step  Actor                  Action / Event                           App Status
────  ─────────────────────  ───────────────────────────────────────  ──────────────
 1–12  (same as S1 steps 1–12 — invitation through offer selection)   PROCESSING
13    Decision Engine        Execute hard pull; return DECLINED        —
14    pricing-orch-svc       PATCH /status → DECLINED                 DECLINED
15    Notification Platform  Send adverse action notice to customer    DECLINED
16    Customer               Log in to portal                          DECLINED
17    UI Shell               Poll status; route to DenialMfe           DECLINED
18    Customer               View denial information and next steps    DECLINED
      (application is terminal — no further actions available)
```

**State transitions:**
```
PROCESSING → DECLINED  (terminal)
```

**Applicant experience on portal:**
- DenialMfe is shown with:
  - "Application not approved" message
  - Adverse action notice information
  - Right to free credit report
  - Dispute process explanation
- No further actions are available in the portal.

---

# 6. Scenario S4 — Direct Application (No Invitation)

**Description:** Customer applies directly without an invitation token. No prefill data is available.

```
Step  Actor                  Action / Event                           App Status
────  ─────────────────────  ───────────────────────────────────────  ──────────────
 1    Customer               Navigate to application URL (no token)   —
 2    UI Shell               Load form with no prefill                —
 3    Customer               Complete all fields manually              —
 4    Customer               Submit form                              —
 5–28  (same as S1 steps 5–28 — application creation through funding) COMPLETED
```

The flow is identical to S1 from step 5 onwards. The absence of an invitation token means:
- No `intakeId` is set on the application
- No pre-application events appear in the timeline
- All form fields require manual entry

---

# 7. Scenario S5 — Applicant Login and Status Check

**Description:** Applicant returns to check their application status at any point after the decision phase.

```
Step  Actor          Action                                Result
────  ─────────────  ────────────────────────────────────  ──────────────────────────
 1    Applicant      Navigate to portal login page         Login form shown
 2    Applicant      Enter applicationId                   —
 3    Applicant      Enter last 4 digits of SSN            —
 4    Applicant      Enter date of birth                   —
 5    Applicant      Submit                                —
 6    app-mgmt-svc   Find applicant by applicationId       Applicant found
 7    app-mgmt-svc   Check application not in terminal state  Non-terminal confirmed
 8    app-mgmt-svc   Verify last4SSN + DOB                 Match confirmed
 9    app-mgmt-svc   Issue JWT (30-min expiry)             sessionToken returned
10    UI Shell       Store session token                   —
11    UI Shell       Poll GET /applications/{id} (every 3s) Current status returned
12    UI Shell       Route to correct MFE                  MFE displayed

Status → MFE routing:
  APPROVED              → OfferAcceptanceMfe
  DECLINED              → DenialMfe
  DOCUMENTS_REQUIRED    → DocumentUploadMfe
  OFFER_ACCEPTED / FUNDING_PENDING / FUNDED / COMPLETED → ConfirmationMfe
  UNDERWRITING          → "Under review" message
  IN_PROGRESS / SUBMITTED / PROCESSING → "Being processed" message
```

**Error paths:**

| Error Condition | HTTP Status | Message Shown |
|----------------|-------------|---------------|
| applicationId not found | 404 | "Application not found" |
| SSN or DOB does not match | 401 | "Verification failed. Please check your details." |
| Application in terminal state | 422 | "This application can no longer be accessed." |

---

# 8. Scenario S6 — Document Upload with Virus Rejection

**Description:** One of the uploaded documents fails the virus scan. The applicant is notified and must re-upload.

```
Step  Actor                  Action / Event                           App Status
────  ─────────────────────  ───────────────────────────────────────  ──────────────
(Requirements already created; applicant on DocumentUploadMfe)
 1    Customer               Upload file for BANK_STATEMENT            DOCUMENTS_REQUIRED
 2    document-service       Store to S3; create DocumentRecord UPLOADED  —
 3    Virus Scanner          Scan result: INFECTED                     —
 4    document-service       POST /internal/virus-scan/result (clean=false)  —
 5    document-service       Mark record REJECTED                      —
 6    document-service       Mark requirement REJECTED                 —
 7    document-service       Publish DocumentRejected event            —
 8    Customer               Poll GET /requirements; slot shows REJECTED  —
 9    Customer               Select replacement file                   —
10    Customer               Upload replacement file                   —
11    document-service       Store to S3; create new DocumentRecord    —
12    Virus Scanner          Scan result: CLEAN                        —
13    document-service       Mark record VERIFIED                      —
14    document-service       Requirement count met → mark COMPLETED    —
15    (continue fulfilling remaining requirements…)                    —
```

**Key rule:** Rejected files count is not included in the satisfied count. The applicant must upload a clean replacement for each rejected slot.

---

# 9. Scenario S7 — Duplicate E-Sign Attempt (Idempotency)

**Description:** Applicant or browser accidentally submits the e-sign form twice.

```
Step  Actor                  Action / Event                           Result
────  ─────────────────────  ───────────────────────────────────────  ──────────────
 1    Customer               Submit e-sign (first attempt)            201 OK
 2    offer-acceptance-svc   Create ESignRecord; publish ESignCompleted  —
 3    app-mgmt-svc           Transition APPROVED → OFFER_ACCEPTED     OFFER_ACCEPTED
 4    Customer               Resubmit (refresh / double-click)        —
 5    offer-acceptance-svc   Find session; status = SIGNED            —
 6    offer-acceptance-svc   Throw AlreadySignedException             409 CONFLICT
 7    UI Shell               Display: already signed message          —
 8    (no duplicate ESignRecord created; no duplicate event published) —
```

The `OfferAcceptanceSession.sign()` method enforces this check at the domain level before any persistence occurs.

---

# 10. State Transition Summary Across Scenarios

```
                      ┌─────────────────────────────────────────────┐
                      │         All Scenarios Entry Point           │
                      │                                             │
                      │  CREATED → STARTED → IN_PROGRESS            │
                      │          → SUBMITTED → PROCESSING           │
                      └──────────────────┬──────────────────────────┘
                                         │
                   ┌─────────────────────┼──────────────────────┐
                   │                     │                      │
                   ▼                     ▼                      ▼
             [DECLINED]           [APPROVED]            [REFERRED]
             (S3, terminal)           │                 (manual UW)
                                      │
                      ┌───────────────┤
                      │               │
                      ▼               ▼
             [DOCUMENTS_REQUIRED]  [direct to e-sign]
             (S2)                       │
              │                         │
              │ DocumentsCompleted       │ ESignCompleted
              ▼                         │
         [UNDERWRITING]                 │
              │ review complete         │
              ▼                         │
          [APPROVED] ◄──────────────────┘
              │
              │ ESignCompleted
              ▼
         [OFFER_ACCEPTED]
              │
              ▼
         [FUNDING_PENDING]
              │
              ▼
           [FUNDED]
              │
              ▼
         [COMPLETED] (terminal)
```

---

# 11. Related Documents

| Document | Location |
|----------|----------|
| Functional Flow | `docs/flows/01-functional-flow.md` |
| Component Interaction Flows | `docs/flows/02-component-interaction-flows.md` |
| Application State Machine | `docs/architecture/002-application-state-machine.md` |
| Architecture Overview | `docs/architecture/000-architecture-overview.md` |
