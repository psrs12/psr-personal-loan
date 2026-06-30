# Document Collection Capability Specification

# Personal Loan Acquisition Platform

Version: 1.0

Status: Active

Capability: Document Collection

Owner: Acquisition Platform

---

# 1. Purpose

The Document Collection capability owns the full document lifecycle for a personal loan application.

When the Decision Engine requires supporting documents before a final decision can be made, this capability receives the document requirements, translates them to platform domain values via an Anti-Corruption Layer (ACL), stores them per application, accepts applicant uploads, tracks upload status through virus scanning, and signals completion to the rest of the platform.

---

# 2. Business Objective

The capability shall:

* Receive document requirements from the Decision Engine via the `FinalDecisionDocumentsRequired` platform event.
* Translate Decision Engine document type codes to platform `DocumentType` values via the ACL.
* Store per-application document requirements with individual lifecycle status.
* Accept document uploads from applicants.
* Track each uploaded document through virus scanning.
* Publish `DocumentsCompleted` when all requirements are satisfied.
* Allow re-upload when a document is rejected by the virus scanner.

---

# 3. Scope

## In Scope

`FinalDecisionDocumentsRequired` event consumption.

ACL mapping from Decision Engine codes to platform `DocumentType`.

Document requirement persistence.

Document requirements retrieval API.

Document upload API and storage.

Virus scan lifecycle tracking.

`DocumentsCompleted` event publication.

Dead-letter handling for unknown document type codes.

---

## Out Of Scope

Decision Engine document type code ownership (codes belong to the Decision Engine — this capability only translates).

Application state ownership.

Offer pricing and decisioning.

Offer acceptance and e-sign.

Funding execution.

---

# 4. Workflow

```
FinalDecisionDocumentsRequired event received
  → Apply ACL mapping: Decision Engine codes → platform DocumentType
  → Persist DocumentRequirement per document (status: PENDING)
  → Call application-management-service: transition to DOCUMENTS_REQUIRED

Applicant uploads document
  → POST /applications/{id}/documents/upload
  → Store in S3
  → Create DocumentRecord (status: UPLOADED)
  → Update DocumentRequirement status to UPLOADED
  → Publish DocumentUploaded event
  → Virus scanner processes document

Virus scan — clean
  → Update DocumentRecord status to VERIFIED
  → Evaluate overall completeness

Virus scan — rejected
  → Update DocumentRecord status to REJECTED
  → Update DocumentRequirement status to REJECTED
  → Publish DocumentRejected event
  → Applicant may re-upload

All requirements satisfied
  → Publish DocumentsCompleted event
  → application-management-service transitions application to UNDERWRITING
```

---

# 5. Domain Model

## DocumentRequirement

```
requirementId       UUID
applicationId       UUID
documentType        DocumentType (platform domain value)
count               Integer
description         String
status              PENDING | UPLOADED | REJECTED | COMPLETED
createdTimestamp    ISO-8601
```

## DocumentRecord

```
documentId          UUID
applicationId       UUID
requirementId       UUID
documentType        DocumentType
storagePath         String (S3 reference)
status              UPLOADED | VERIFIED | REJECTED
uploadedTimestamp   ISO-8601
verifiedTimestamp   ISO-8601
```

## DocumentType (Platform Domain Values)

| Value | Description |
|-------|-------------|
| `BANK_STATEMENT` | Bank statement |
| `PAYSLIP` | Payslip |
| `PROOF_OF_ADDRESS` | Utility bill or council tax letter |
| `PROOF_OF_IDENTITY` | Passport or driving licence |
| `TAX_RETURN` | Tax return document |

---

# 6. ACL — Decision Engine Document Code Mapping

`document-service` is the sole owner of the mapping between Decision Engine document type codes and platform `DocumentType` values.

The mapping lives in `DecisionEngineDocumentCodeMapper`. No other service may contain Decision Engine vocabulary.

### Current Mapping

| Decision Engine Code | Platform DocumentType | Count | Description |
|---------------------|----------------------|-------|-------------|
| `BANK_STMT_3M` | `BANK_STATEMENT` | 3 | Last 3 months of bank statements |
| `PAYSLIP_2` | `PAYSLIP` | 2 | Last 2 payslips |
| `POA` | `PROOF_OF_ADDRESS` | 1 | Proof of address (utility bill or council tax) |
| `POI` | `PROOF_OF_IDENTITY` | 1 | Proof of identity (passport or driving licence) |
| `TAX_RTN` | `TAX_RETURN` | 1 | Most recent tax return |

### Mapping Rules

- If a received code is not in the mapping, the event is routed to the dead-letter queue and an `UnknownDocumentTypeAlert` is emitted for operational monitoring.
- No partial requirements are persisted when an unknown code is encountered — the entire event is rejected.
- When the Decision Engine publishes a new contract version, the ACL mapping must be updated before the new version is activated.
- In-flight applications continue to use the mapping version active at the time their requirements were stored.

---

# 7. Functional Requirements

## FR-001 Document Requirements Reception

- **WHEN** a `FinalDecisionDocumentsRequired` event is received
- **THEN** the system SHALL apply the ACL mapping to each `decisionEngineCode`
- **THEN** a `DocumentRequirement` record SHALL be persisted per document type with status `PENDING`
- **THEN** the system SHALL call `application-management-service` to transition the application to `DOCUMENTS_REQUIRED`

### Unknown Code Handling

- **WHEN** any code in the event is not found in the ACL mapping
- **THEN** no requirements SHALL be persisted
- **THEN** the event SHALL be routed to the dead-letter queue
- **THEN** an `UnknownDocumentTypeAlert` SHALL be emitted

---

## FR-002 Document Requirements API

`GET /applications/{applicationId}/documents/requirements`

Response:

```json
{
  "applicationId": "APP123",
  "requirements": [
    {
      "requirementId": "req-001",
      "documentType": "BANK_STATEMENT",
      "count": 3,
      "description": "Last 3 months of bank statements",
      "status": "PENDING"
    },
    {
      "requirementId": "req-002",
      "documentType": "PAYSLIP",
      "count": 2,
      "description": "Last 2 payslips",
      "status": "UPLOADED"
    }
  ]
}
```

### Error Codes

| Code | HTTP | Condition |
|------|------|-----------|
| `DOCUMENT_REQUIREMENTS_NOT_FOUND` | 404 | No requirements exist for this application |

---

## FR-003 Document Upload

`POST /applications/{applicationId}/documents/upload` — multipart form data.

Fields: `documentType` (string), `file` (binary).

### Success

- Document stored in S3.
- `DocumentRecord` created with status `UPLOADED`.
- Corresponding `DocumentRequirement` status updated to `UPLOADED`.
- `DocumentUploaded` event published.

### Rejection Cases

| Code | HTTP | Condition |
|------|------|-----------|
| `DOCUMENT_TYPE_NOT_REQUIRED` | 422 | `documentType` not in this application's requirements |
| `DOCUMENT_COUNT_SATISFIED` | 422 | Required count for this `documentType` is already fulfilled |

---

## FR-004 Virus Scan Lifecycle

### Scan Passes

- `DocumentRecord` status → `VERIFIED`
- Overall completion is evaluated

### Scan Fails

- `DocumentRecord` status → `REJECTED`
- `DocumentRequirement` status → `REJECTED`
- `DocumentRejected` event published
- Applicant is permitted to re-upload for the same requirement

---

## FR-005 Completion Detection

- **WHEN** all `DocumentRequirement` records for an application reach `COMPLETED` status
- **THEN** the system SHALL publish a `DocumentsCompleted` event
- **THEN** `application-management-service` SHALL transition the application to `UNDERWRITING`

---

# 8. Events Published

| Event | Trigger | Topic |
|-------|---------|-------|
| `DocumentUploaded` | Document stored and `DocumentRecord` created | `document.uploaded` |
| `DocumentRejected` | Virus scan rejects document | `document.rejected` |
| `DocumentsCompleted` | All requirements satisfied | `document.completed` |

### DocumentsCompleted Payload

```json
{
  "applicationId": "APP123",
  "completedAt": "2026-06-30T14:30:00Z",
  "correlationId": "corr-abc-123"
}
```

---

# 9. Events Consumed

| Event | Source | Action |
|-------|--------|--------|
| `FinalDecisionDocumentsRequired` | `pricing-orchestration-service` | Create document requirements |

---

# 10. External Event Contract

The `FinalDecisionDocumentsRequired` event contract between the Decision Engine and `document-service` is versioned at:

```
docs/contracts/decision-engine-events/v1/documents-required.json
```

Do not change document type codes without updating this contract file and the ACL mapping in `DecisionEngineDocumentCodeMapper`.

---

# 11. API Summary

| Method | Path | Purpose |
|--------|------|---------|
| `GET` | `/applications/{id}/documents/requirements` | Retrieve document requirements |
| `POST` | `/applications/{id}/documents/upload` | Upload a document |

All endpoints require `Authorization: Bearer <token>`.

---

# 12. UI Component

The `<document-upload-manager>` web component (served by `document-management-ui`) handles:

- Display of document requirements with status indicators.
- Per-requirement file upload with progress tracking.
- Status refresh after upload and after virus scan completion.

---

# 13. Acceptance Criteria

The capability is complete when:

- `FinalDecisionDocumentsRequired` event creates `DocumentRequirement` records with status `PENDING`.
- ACL mapping correctly translates all known Decision Engine codes to platform `DocumentType` values.
- Unknown codes route the event to the dead-letter queue and emit an alert — no partial requirements are stored.
- Document requirements API returns all requirements with correct status.
- Document upload stores the file and creates a `DocumentRecord` with status `UPLOADED`.
- Upload is rejected with `DOCUMENT_TYPE_NOT_REQUIRED` for unknown document types.
- Upload is rejected with `DOCUMENT_COUNT_SATISFIED` when the count is already fulfilled.
- Clean virus scan updates `DocumentRecord` to `VERIFIED`.
- Failed virus scan updates status to `REJECTED` and publishes `DocumentRejected`.
- Re-upload is permitted after a rejection.
- `DocumentsCompleted` is published when all requirements are satisfied.
- `application-management-service` transitions the application to `UNDERWRITING` on `DocumentsCompleted`.

---

# 14. Related Documents

```
docs/architecture/005-event-driven-architecture.md
docs/architecture/006-integration-patterns.md
docs/contracts/decision-engine-events/v1/documents-required.json
openspec/changes/post-decision-flow/specs/document-collection/spec.md
openspec/application-management/spec.md
```
