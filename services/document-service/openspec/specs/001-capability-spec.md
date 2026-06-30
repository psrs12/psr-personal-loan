# 001 - Document Collection Capability Specification

Version: 1.0

Status: Active

Service: document-service

Port: 8084

---

# 1. Purpose

The Document Service owns the complete document lifecycle for a personal loan application.

When the Decision Engine requires supporting documents, this service:

- Consumes the `FinalDecisionDocumentsRequired` event from `pricing-orchestration-service`
- Applies the Anti-Corruption Layer (ACL) to translate Decision Engine codes to platform `DocumentType` values
- Persists `DocumentRequirement` records for the application
- Accepts document uploads from the applicant (stored in S3)
- Tracks each document through virus scanning (UPLOADED → SCANNING → VERIFIED | REJECTED)
- Detects when all requirements are satisfied and publishes `DocumentsCompleted`

---

# 2. Bounded Context

Document Collection

The service does not own application state. State transitions are requested via `ApplicationManagementPort` (REST adapter calling `PATCH /applications/{id}/status`).

---

# 3. Responsibilities

| Responsibility | Owned |
|----------------|-------|
| DocumentRequirement lifecycle | Yes |
| DocumentRecord lifecycle | Yes |
| ACL mapping (Decision Engine → platform DocumentType) | Yes |
| S3 storage of uploaded files | Yes |
| Virus scan callback handling | Yes |
| DocumentsCompleted detection and event publication | Yes |
| Application state transitions | No — delegated via ApplicationManagementPort |
| Decision Engine document type vocabulary | No — received verbatim via event |

---

# 4. Document Types (Platform Domain)

```
BANK_STATEMENT
PAY_SLIP
TAX_RETURN
GOVERNMENT_ID
PROOF_OF_ADDRESS
EMPLOYMENT_LETTER
```

---

# 5. ACL — Decision Engine Code Mapping

Owned by `DecisionEngineDocumentCodeMapper`. This is the only class in the platform permitted to contain Decision Engine document type vocabulary.

| Decision Engine Code | Platform DocumentType | Count | Description |
|---------------------|----------------------|-------|-------------|
| `BANK_STMT_3M` | `BANK_STATEMENT` | 3 | Last 3 months of bank statements |
| `BANK_STMT_6M` | `BANK_STATEMENT` | 6 | Last 6 months of bank statements |
| `PAYSLIP_2` | `PAY_SLIP` | 2 | Last 2 payslips |
| `PAYSLIP_3` | `PAY_SLIP` | 3 | Last 3 payslips |
| `TAX_RETURN_1Y` | `TAX_RETURN` | 1 | Most recent tax return |
| `TAX_RETURN_2Y` | `TAX_RETURN` | 2 | Last 2 years tax returns |
| `GOV_ID` | `GOVERNMENT_ID` | 1 | Government-issued photo ID |
| `PROOF_OF_ADDRESS` | `PROOF_OF_ADDRESS` | 1 | Proof of current address |
| `EMPLOYMENT_LETTER` | `EMPLOYMENT_LETTER` | 1 | Employer confirmation letter |

Unknown codes throw `UnknownDocumentTypeException` and route the event to the dead-letter queue.

---

# 6. Document Requirement Status Values

| Status | Meaning |
|--------|---------|
| `PENDING` | Requirement created, no upload yet |
| `UPLOADED` | At least one document uploaded, awaiting virus scan result |
| `REJECTED` | Uploaded document failed virus scan; re-upload permitted |
| `COMPLETED` | Required count satisfied with VERIFIED documents |

---

# 7. Document Record Status Values

| Status | Meaning |
|--------|---------|
| `UPLOADED` | File stored in S3, queued for virus scan |
| `SCANNING` | Virus scan in progress |
| `VERIFIED` | Virus scan passed |
| `REJECTED` | Virus scan failed |

---

# 8. Workflow Summary

```
FinalDecisionDocumentsRequired (Kafka)
  → FinalDecisionDocumentsRequiredConsumer
  → StoreDocumentRequirementsUseCase
  → ACL mapping per code
  → DocumentRequirement records persisted (status: PENDING)
  → ApplicationManagementPort: PATCH status → DOCUMENTS_REQUIRED

POST /applications/{id}/documents/upload
  → UploadDocumentUseCase
  → Validate documentType in requirements
  → Validate count not already satisfied
  → S3StorageAdapter: store file → storageReference
  → DocumentRecord created (status: UPLOADED)
  → DocumentRequirement status → UPLOADED
  → Publish DocumentUploaded

POST /internal/virus-scan/result
  → ProcessVirusScanResultUseCase
  → clean=true: DocumentRecord → VERIFIED, check completeness
  → clean=false: DocumentRecord → REJECTED, DocumentRequirement → REJECTED
  → If all requirements COMPLETED: publish DocumentsCompleted
    → ApplicationManagementPort: PATCH status → UNDERWRITING
```

---

# 9. Related Documents

```
openspec/document-collection/spec.md                          — platform capability spec
openspec/changes/post-decision-flow/specs/document-collection/spec.md  — change delta spec
docs/contracts/decision-engine-events/v1/documents-required.json
services/document-service/openspec/specs/02-domain-model.md
services/document-service/openspec/specs/04-sequence-diagrams.md
services/document-service/openspec/specs/05-api-contracts.md
services/document-service/openspec/specs/06-persistence-model.md
services/document-service/openspec/specs/07-acceptance-tests.md
```
