## Why

The Personal Loan Acquisition Platform has no capability to enforce regulatory compliance obligations at the points in the acquisition flow where they are legally required — specifically FCRA credit pull consent, FCRA/ECOA adverse action notices, TILA disclosure audit, and AML/sanctions screening. Without compliance gates, the platform cannot operate in a production banking environment and exposes the business to regulatory and legal risk.

## What Changes

- Introduce `compliance-orchestration-service` as a new bounded context owning compliance gate execution, consent audit records, adverse action notice generation, disclosure audit records, and compliance hold management
- Add a **pre-screening gate** (Gate 1) after `ApplicationSubmitted` — AML/sanctions check via the enterprise Fraud/AML Platform before the soft pull is initiated
- Add a **credit pull consent audit** (Gate 2) — synchronous FCRA consent record written and confirmed before `pricing-orchestration-service` initiates the hard pull
- Add an **adverse action gate** (Gate 3) — on `FinalDecisionDeclined`, generate an FCRA/ECOA-compliant adverse action notice with reason codes and timing, delivered via the Notification Platform
- Add a **disclosure audit** (Gate 4) — record that TILA-required fields (APR, total of payments, finance charge, loan term) were presented and accepted as part of the offer-acceptance e-sign flow
- Add a **pre-funding compliance hold** (Gate 5) — AML/sanctions re-check via the enterprise Fraud/AML Platform before the funding request is submitted; hold the application if the check fails
- Extend `pricing-orchestration-service` event flow: Gate 2 confirmation event must be received before hard pull is initiated (**BREAKING** for hard pull trigger)
- Extend `offer-acceptance-service` e-sign flow: TILA disclosure fields must be present in the e-sign payload for Gate 4 audit record

## Capabilities

### New Capabilities

- `aml-screening`: Pre-screening (Gate 1) and pre-funding (Gate 5) AML/sanctions checks via the enterprise Fraud/AML Platform; manages hold states and resume/escalation
- `fcra-consent-audit`: Credit pull consent record creation and retrieval (Gate 2); synchronous gate that must confirm before hard pull fires
- `adverse-action`: FCRA/ECOA adverse action notice generation and delivery on declined decisions (Gate 3); owns reason code mapping and mandatory timing rules
- `tila-disclosure-audit`: Records that APR, total of payments, finance charge, and loan term were presented and accepted at e-sign (Gate 4); audit record is immutable once written

### Modified Capabilities

- `pricing-orchestration`: Hard pull initiation must wait for `FcraConsentAuditConfirmed` event (Gate 2 integration)
- `offer-acceptance`: E-sign payload must include TILA disclosure fields (APR, totalOfPayments, financeCharge, loanTerm) for Gate 4 audit record creation

## Impact

- **New service**: `compliance-orchestration-service` — owns all four compliance gate implementations behind port interfaces
- **Extended service**: `pricing-orchestration-service` — hard pull trigger becomes event-driven on `FcraConsentAuditConfirmed` rather than direct invocation
- **Extended service**: `offer-acceptance-service` — `POST /applications/{id}/esign` request body extended with TILA disclosure fields
- **External dependency**: Enterprise Fraud/AML Platform — synchronous REST adapter for Gates 1 and 5
- **External dependency**: Notification Platform — delivers adverse action notice for Gate 3
- **New domain events**: `AmlScreeningPassed`, `AmlScreeningFailed`, `AmlHoldPlaced`, `FcraConsentAuditConfirmed`, `AdverseActionIssued`, `TilaDisclosureAuditCreated`, `PreFundingCompliancePassed`, `PreFundingComplianceHeld`
- **Regulatory scope**: FCRA, ECOA/Regulation B, TILA/Regulation Z, Bank Secrecy Act (AML)
