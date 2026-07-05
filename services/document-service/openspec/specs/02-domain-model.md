# Domain Model

# Document Service

Version: 1.0

---

# Bounded Context

Document Collection

---

# Aggregate: DocumentRequirement

Represents a single document type required by the Decision Engine for an application.

One `DocumentRequirement` per document type per application. Multiple requirements may exist per application.

Attributes:

* requirementId (UUID)
* applicationId (UUID)
* documentType (DocumentType)
* count (int) — number of documents required for this type
* description (String)
* status (DocumentRequirementStatus)
* createdAt (LocalDateTime)

## Factory

`DocumentRequirement.create(applicationId, documentType, count, description)` — assigns UUID, sets status `PENDING`.

`DocumentRequirement.reconstitute(...)` — rehydrates from persistence.

## Behaviour

```
markUploaded()   → status = UPLOADED
markRejected()   → status = REJECTED
markCompleted()  → status = COMPLETED
```

---

# Aggregate: DocumentRecord

Represents a single uploaded file for a requirement.

Attributes:

* documentId (UUID)
* applicationId (UUID)
* requirementId (UUID)
* documentType (DocumentType)
* storageReference (String) — S3 key
* status (DocumentRecordStatus)
* uploadedAt (LocalDateTime)

## Factory

`DocumentRecord.create(applicationId, requirementId, documentType, storageReference)` — assigns UUID, sets status `UPLOADED`.

`DocumentRecord.reconstitute(...)` — rehydrates from persistence.

## Behaviour

```
markVerified()  → status = VERIFIED
markRejected()  → status = REJECTED
markScanning()  → status = SCANNING
```

---

# Enum: DocumentType

Platform domain values — not Decision Engine codes.

```
BANK_STATEMENT
PAY_SLIP
TAX_RETURN
GOVERNMENT_ID
PROOF_OF_ADDRESS
EMPLOYMENT_LETTER
```

---

# Enum: DocumentRequirementStatus

```
PENDING     — no upload yet
UPLOADED    — file received, virus scan pending
REJECTED    — virus scan failed; re-upload permitted
COMPLETED   — required count satisfied with VERIFIED documents
```

---

# Enum: DocumentRecordStatus

```
UPLOADED    — file in S3, scan not started
SCANNING    — virus scan in progress
VERIFIED    — scan passed
REJECTED    — scan failed
```

---

# Domain Event: FinalDecisionDocumentsRequiredEvent

Consumed from Kafka. Carries verbatim Decision Engine codes. The domain layer never sees raw codes — the ACL translates them before the event reaches the use case.

Fields consumed:

* applicationId (UUID)
* correlationId (String)
* documents (List of {decisionEngineCode: String, count: int})

---

# Ports (Domain Interfaces)

## DocumentRequirementRepository

```
void save(DocumentRequirement requirement)
List<DocumentRequirement> findByApplicationId(UUID applicationId)
Optional<DocumentRequirement> findByApplicationIdAndDocumentType(UUID applicationId, DocumentType type)
List<DocumentRequirement> findAllByApplicationId(UUID applicationId)
```

## DocumentRecordRepository

```
void save(DocumentRecord record)
Optional<DocumentRecord> findById(UUID documentId)
List<DocumentRecord> findByRequirementId(UUID requirementId)
```

## StoragePort

```
String store(UUID applicationId, DocumentType documentType, MultipartFile file)
```

Returns: S3 storage reference (key).

## ApplicationManagementPort

```
void updateApplicationStatus(UUID applicationId, String status)
```

Called to set `DOCUMENTS_REQUIRED` after requirements are stored, and `UNDERWRITING` after `DocumentsCompleted`.

## DocumentEventPublisher

```
void publishDocumentUploaded(UUID applicationId, UUID documentId, DocumentType documentType)
void publishDocumentRejected(UUID applicationId, UUID documentId, String failureReason)
void publishDocumentsCompleted(UUID applicationId)
```

---

# ACL Component: DecisionEngineDocumentCodeMapper

Infrastructure component (not a domain object). Translates Decision Engine codes to platform DocumentType values with count and description.

```java
MappedDocument map(String decisionEngineCode)
// throws UnknownDocumentTypeException if code not in mapping
```

```java
record MappedDocument(DocumentType documentType, int count, String description) {}
```

---

# Domain Exceptions

| Exception | Thrown When | HTTP Code |
|-----------|------------|-----------|
| `DocumentTypeNotRequiredException` | Uploaded documentType not in application's requirements | 422 |
| `DocumentCountSatisfiedException` | Required count for documentType already fulfilled | 422 |
| `UnknownDocumentTypeException` | Decision Engine code not in ACL mapping | Routes to DLQ |
