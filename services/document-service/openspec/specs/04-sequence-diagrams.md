# Sequence Diagrams

# Document Service

Version: 1.0

---

# 1. Document Requirements Stored on Decision Event

```mermaid
sequenceDiagram
    participant Pricing as pricing-orchestration-service
    participant Kafka
    participant DS as document-service
    participant ACL as DecisionEngineDocumentCodeMapper
    participant DB as document DB
    participant AMS as application-management-service

    Pricing ->> Kafka: FinalDecisionDocumentsRequired {applicationId, documents[]}
    Kafka ->> DS: FinalDecisionDocumentsRequiredConsumer
    DS ->> ACL: map(decisionEngineCode) for each document
    ACL -->> DS: MappedDocument {documentType, count, description}
    DS ->> DB: save DocumentRequirement per type (status: PENDING)
    DB -->> DS: saved
    DS ->> AMS: PATCH /applications/{id}/status → DOCUMENTS_REQUIRED
    AMS -->> DS: 200 OK
```

---

# 2. Unknown Document Code — Dead-Letter

```mermaid
sequenceDiagram
    participant Kafka
    participant DS as document-service
    participant ACL as DecisionEngineDocumentCodeMapper
    participant DLQ as Dead-Letter Queue

    Kafka ->> DS: FinalDecisionDocumentsRequired {unknown code}
    DS ->> ACL: map("UNKNOWN_CODE")
    ACL -->> DS: UnknownDocumentTypeException
    DS ->> DLQ: route event
    DS ->> DS: emit UnknownDocumentTypeAlert (operational monitoring)
```

---

# 3. Applicant Retrieves Document Requirements

```mermaid
sequenceDiagram
    actor Applicant
    participant UI as document-management-ui
    participant DS as document-service
    participant DB as document DB

    Applicant ->> UI: View document requirements
    UI ->> DS: GET /applications/{id}/documents/requirements
    DS ->> DB: findByApplicationId
    DB -->> DS: List<DocumentRequirement>
    DS -->> UI: List<DocumentRequirementResponse>
    UI -->> Applicant: Display requirements with status
```

---

# 4. Successful Document Upload

```mermaid
sequenceDiagram
    actor Applicant
    participant UI as document-management-ui
    participant DS as document-service
    participant DB as document DB
    participant S3 as S3 Storage
    participant Kafka
    participant Scanner as Virus Scanner

    Applicant ->> UI: Upload file for BANK_STATEMENT
    UI ->> DS: POST /applications/{id}/documents/upload {documentType, file}
    DS ->> DB: findByApplicationIdAndDocumentType (validate requirement exists)
    DB -->> DS: DocumentRequirement (status: PENDING)
    DS ->> DS: validate count not satisfied
    DS ->> S3: store(applicationId, documentType, file)
    S3 -->> DS: storageReference
    DS ->> DB: save DocumentRecord (status: UPLOADED)
    DS ->> DB: update DocumentRequirement status → UPLOADED
    DS ->> Kafka: DocumentUploaded {applicationId, documentId, documentType}
    DS -->> UI: 201 Created {documentId, status: UPLOADED}
    UI -->> Applicant: Upload confirmed
    Note over Scanner: Async virus scan triggered
```

---

# 5. Virus Scan — Clean

```mermaid
sequenceDiagram
    participant Scanner as Virus Scanner
    participant DS as document-service
    participant DB as document DB
    participant Kafka
    participant AMS as application-management-service

    Scanner ->> DS: POST /internal/virus-scan/result {documentId, clean: true}
    DS ->> DB: findDocumentRecord(documentId)
    DB -->> DS: DocumentRecord (status: UPLOADED)
    DS ->> DB: update DocumentRecord status → VERIFIED
    DS ->> DB: evaluate all DocumentRequirements for application
    alt All requirements COMPLETED
        DS ->> Kafka: DocumentsCompleted {applicationId}
        DS ->> AMS: PATCH /applications/{id}/status → UNDERWRITING
    else Not all complete
        DS -->> Scanner: 200 OK (no further action)
    end
```

---

# 6. Virus Scan — Rejected

```mermaid
sequenceDiagram
    participant Scanner as Virus Scanner
    participant DS as document-service
    participant DB as document DB
    participant Kafka

    Scanner ->> DS: POST /internal/virus-scan/result {documentId, clean: false, failureReason}
    DS ->> DB: findDocumentRecord(documentId)
    DB -->> DS: DocumentRecord
    DS ->> DB: update DocumentRecord status → REJECTED
    DS ->> DB: update DocumentRequirement status → REJECTED
    DS ->> Kafka: DocumentRejected {applicationId, documentId, failureReason}
    DS -->> Scanner: 200 OK
    Note over DS: Applicant may re-upload for this requirement
```

---

# 7. Upload Rejected — Document Type Not Required

```mermaid
sequenceDiagram
    actor Applicant
    participant UI
    participant DS as document-service
    participant DB as document DB

    Applicant ->> UI: Upload file for GOVERNMENT_ID
    UI ->> DS: POST /applications/{id}/documents/upload {documentType: GOVERNMENT_ID, file}
    DS ->> DB: findByApplicationIdAndDocumentType(GOVERNMENT_ID)
    DB -->> DS: empty (not in requirements)
    DS -->> UI: 422 DOCUMENT_TYPE_NOT_REQUIRED
    UI -->> Applicant: Display error
```
