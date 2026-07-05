# Acceptance Tests

# Document Service

Version: 1.0

---

# Feature: Document Requirements Reception

## Scenario 1 — Requirements stored on FinalDecisionDocumentsRequired

**Given** a `FinalDecisionDocumentsRequired` event is received for `APP123` with codes `BANK_STMT_3M` and `PAYSLIP_2`

**When** `FinalDecisionDocumentsRequiredConsumer` processes the event

**Then** a `DocumentRequirement` shall exist for `APP123` with `documentType: BANK_STATEMENT`, `count: 3`, `status: PENDING`

**And** a `DocumentRequirement` shall exist for `APP123` with `documentType: PAY_SLIP`, `count: 2`, `status: PENDING`

**And** `application-management-service` shall receive `PATCH /applications/APP123/status → DOCUMENTS_REQUIRED`

---

## Scenario 2 — Unknown Decision Engine code routes to dead-letter

**Given** a `FinalDecisionDocumentsRequired` event contains an unknown code `UNKNOWN_CODE`

**When** `FinalDecisionDocumentsRequiredConsumer` processes the event

**Then** no `DocumentRequirement` shall be persisted for `APP123`

**And** the event shall be routed to the dead-letter queue

**And** an `UnknownDocumentTypeAlert` shall be emitted

---

# Feature: Document Requirements Retrieval

## Scenario 3 — Requirements returned for application

**Given** `DocumentRequirement` records exist for `APP123`

**When** `GET /applications/APP123/documents/requirements` is called

**Then** the response shall contain all requirements with `requirementId`, `documentType`, `count`, `description`, `status`

---

# Feature: Document Upload

## Scenario 4 — Successful upload

**Given** a `DocumentRequirement` of type `BANK_STATEMENT` with `count: 3` exists for `APP123` with status `PENDING`

**When** `POST /applications/APP123/documents/upload` is called with `documentType: BANK_STATEMENT` and a valid file

**Then** the file shall be stored in S3 and a `storageReference` shall be returned

**And** a `DocumentRecord` shall be created with status `UPLOADED`

**And** the `DocumentRequirement` status shall be updated to `UPLOADED`

**And** a `DocumentUploaded` event shall be published

**And** the response shall be `201 Created` with `documentId` and `status: UPLOADED`

---

## Scenario 5 — Upload rejected — document type not required

**Given** no `DocumentRequirement` for `GOVERNMENT_ID` exists for `APP123`

**When** `POST /applications/APP123/documents/upload` is called with `documentType: GOVERNMENT_ID`

**Then** the response shall be `422` with error code `DOCUMENT_TYPE_NOT_REQUIRED`

**And** no file shall be stored and no `DocumentRecord` created

---

## Scenario 6 — Upload rejected — count already satisfied

**Given** a `DocumentRequirement` for `PAY_SLIP` with `count: 2` exists for `APP123`

**And** two VERIFIED `DocumentRecord` entries already exist for this requirement

**When** `POST /applications/APP123/documents/upload` is called with `documentType: PAY_SLIP`

**Then** the response shall be `422` with error code `DOCUMENT_COUNT_SATISFIED`

---

# Feature: Virus Scan Lifecycle

## Scenario 7 — Document passes virus scan — not yet complete

**Given** a `DocumentRecord` exists for document `DOC123` with status `UPLOADED`

**And** not all requirements are satisfied after this scan

**When** `POST /internal/virus-scan/result` is called with `documentId: DOC123`, `clean: true`

**Then** the `DocumentRecord` status shall be `VERIFIED`

**And** no `DocumentsCompleted` event shall be published

---

## Scenario 8 — All requirements satisfied — DocumentsCompleted published

**Given** all `DocumentRequirement` records for `APP123` are `COMPLETED` after the last virus scan passes

**When** `POST /internal/virus-scan/result` is called with `clean: true` for the final document

**Then** a `DocumentsCompleted` event shall be published to `document.completed`

**And** `application-management-service` shall receive `PATCH /applications/APP123/status → UNDERWRITING`

---

## Scenario 9 — Document fails virus scan

**Given** a `DocumentRecord` exists for document `DOC456` with status `UPLOADED`

**When** `POST /internal/virus-scan/result` is called with `documentId: DOC456`, `clean: false`, `failureReason: "Malware detected"`

**Then** the `DocumentRecord` status shall be `REJECTED`

**And** the corresponding `DocumentRequirement` status shall be `REJECTED`

**And** a `DocumentRejected` event shall be published

**And** the applicant shall be permitted to re-upload for the same requirement

---

# Non-Functional Acceptance Criteria

## Performance

- `GET /documents/requirements` shall respond within 500ms
- `POST /documents/upload` shall respond within 2 seconds (including S3 store)

## Security

- `Authorization: Bearer <token>` required on all applicant-facing endpoints
- `/internal/virus-scan/result` is network-restricted — not exposed publicly
- No PII in log output
- S3 keys must not be returned in API responses

## Idempotency

- Duplicate `FinalDecisionDocumentsRequired` events for the same application shall not create duplicate `DocumentRequirement` records

---

# Related Documents

```
services/document-service/openspec/specs/001-capability-spec.md
services/document-service/openspec/specs/02-domain-model.md
services/document-service/openspec/specs/04-sequence-diagrams.md
services/document-service/openspec/specs/05-api-contracts.md
services/document-service/openspec/specs/06-persistence-model.md
openspec/document-collection/spec.md
docs/contracts/decision-engine-events/v1/documents-required.json
```
