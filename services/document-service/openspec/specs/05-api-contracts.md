# API Contracts

# Document Service

Version: 1.0

Base path: `/applications/{applicationId}/documents`

---

# 1. Get Document Requirements

## GET `/applications/{applicationId}/documents/requirements`

Returns the document requirements for the application.

### Headers

```
Authorization: Bearer <session-token>
X-Correlation-ID: <uuid>
```

### Response — 200 OK

```json
[
  {
    "requirementId": "a1b2c3d4-...",
    "documentType": "BANK_STATEMENT",
    "count": 3,
    "description": "Last 3 months of bank statements",
    "status": "PENDING"
  },
  {
    "requirementId": "e5f6a7b8-...",
    "documentType": "PAY_SLIP",
    "count": 2,
    "description": "Last 2 payslips",
    "status": "UPLOADED"
  }
]
```

### Response — Empty

Returns an empty array `[]` when no requirements exist for the application.

---

# 2. Upload Document

## POST `/applications/{applicationId}/documents/upload`

Uploads a document file for a specific document type.

Content-Type: `multipart/form-data`

### Headers

```
Authorization: Bearer <session-token>
X-Correlation-ID: <uuid>
Content-Type: multipart/form-data
```

### Form Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `documentType` | String | Yes | Platform DocumentType value (e.g. `BANK_STATEMENT`) |
| `file` | MultipartFile | Yes | Document file |

### Response — 201 Created

```json
{
  "documentId": "f1e2d3c4-...",
  "status": "UPLOADED"
}
```

### Error Responses

| Code | Error Code | Condition |
|------|-----------|-----------|
| 422 | `DOCUMENT_TYPE_NOT_REQUIRED` | `documentType` not in this application's requirements |
| 422 | `DOCUMENT_COUNT_SATISFIED` | Required count already fulfilled for this document type |

---

# 3. Virus Scan Callback (Internal)

## POST `/internal/virus-scan/result`

Callback endpoint for the virus scanner. Not exposed publicly.

### Request Body

```json
{
  "documentId": "f1e2d3c4-...",
  "clean": true,
  "failureReason": null
}
```

| Field | Type | Description |
|-------|------|-------------|
| `documentId` | UUID (required) | Document to update |
| `clean` | boolean | `true` = passed, `false` = rejected |
| `failureReason` | String (nullable) | Scan failure reason when `clean: false` |

### Response — 200 OK

Empty body.

---

# 4. Error Response Format

```json
{
  "type": "about:blank",
  "title": "Unprocessable Entity",
  "status": 422,
  "detail": "Document type GOVERNMENT_ID is not required for this application",
  "instance": "/applications/APP123/documents/upload"
}
```

---

# 5. Kafka Events

## Consumed

| Topic | Event | Action |
|-------|-------|--------|
| `pricing.final-decision` | `FinalDecisionDocumentsRequired` | Store document requirements, request DOCUMENTS_REQUIRED status |

## Published

| Topic | Event | Trigger |
|-------|-------|---------|
| `document.uploaded` | `DocumentUploaded` | File stored and DocumentRecord created |
| `document.rejected` | `DocumentRejected` | Virus scan failed |
| `document.completed` | `DocumentsCompleted` | All requirements satisfied |

### DocumentsCompleted Payload

```json
{
  "applicationId": "APP123",
  "correlationId": "corr-abc-123"
}
```

### DocumentRejected Payload

```json
{
  "applicationId": "APP123",
  "documentId": "f1e2d3c4-...",
  "documentType": "BANK_STATEMENT",
  "failureReason": "Malware detected",
  "correlationId": "corr-abc-123"
}
```

---

# 6. HTTP Status Codes

| Code | Meaning |
|------|---------|
| 200 | Success |
| 201 | Document uploaded |
| 422 | Validation failure (wrong type or count satisfied) |
| 500 | Unexpected server error |
