# API Contracts

# Offer Acceptance Service

Version: 1.0

Base path: `/applications/{applicationId}`

---

# 1. Get Declarations

## GET `/applications/{applicationId}/declarations`

Returns the list of declarations attached to the offer acceptance session for the given application.

### Headers

```
Authorization: Bearer <session-token>
X-Correlation-ID: <uuid>
```

### Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `applicationId` | UUID | Yes | Application identifier |

### Response — 200 OK

```json
[
  {
    "declarationId": "d1a2b3c4-...",
    "declarationType": "TERMS_AND_CONDITIONS",
    "title": "Terms and Conditions",
    "content": "I agree to the loan terms and conditions as presented.",
    "mandatory": true
  },
  {
    "declarationId": "e5f6a7b8-...",
    "declarationType": "PRIVACY_POLICY",
    "title": "Privacy Policy",
    "content": "I consent to the collection and use of my personal data.",
    "mandatory": true
  },
  {
    "declarationId": "c9d0e1f2-...",
    "declarationType": "CREDIT_REPORTING",
    "title": "Credit Reporting Consent",
    "content": "I consent to credit reporting agencies being contacted.",
    "mandatory": true
  },
  {
    "declarationId": "a3b4c5d6-...",
    "declarationType": "ELECTRONIC_SIGNATURE",
    "title": "Electronic Signature Consent",
    "content": "I agree that my electronic signature is legally binding.",
    "mandatory": true
  },
  {
    "declarationId": "f7a8b9c0-...",
    "declarationType": "MARKETING",
    "title": "Marketing Communications",
    "content": "I would like to receive marketing communications.",
    "mandatory": false
  }
]
```

### Error Responses

| Code | Error Code | Condition |
|------|-----------|-----------|
| 404 | `ACCEPTANCE_SESSION_NOT_FOUND` | No session exists for this applicationId |

---

# 2. Submit E-Sign

## POST `/applications/{applicationId}/esign`

Captures the applicant's electronic signature. All mandatory declaration IDs must be included.

### Headers

```
Authorization: Bearer <session-token>
X-Correlation-ID: <uuid>
Content-Type: application/json
```

### Request Body

```json
{
  "acceptedDeclarationIds": [
    "d1a2b3c4-...",
    "e5f6a7b8-...",
    "c9d0e1f2-...",
    "a3b4c5d6-..."
  ]
}
```

`acceptedDeclarationIds` must be non-empty (`@NotEmpty`). IP address is captured server-side from `HttpServletRequest.getRemoteAddr()`.

### Response — 200 OK

```json
{
  "eSignId": "f1e2d3c4-...",
  "signedAt": "2026-06-30T10:15:00"
}
```

### Error Responses

| Code | Error Code | Condition |
|------|-----------|-----------|
| 404 | `ACCEPTANCE_SESSION_NOT_FOUND` | No session exists for this applicationId |
| 409 | `ESIGN_ALREADY_COMPLETED` | Session is already SIGNED |
| 422 | `MANDATORY_DECLARATION_NOT_ACCEPTED` | One or more mandatory declarations absent from request |

---

# 3. Error Response Format

```json
{
  "type": "about:blank",
  "title": "Unprocessable Entity",
  "status": 422,
  "detail": "Mandatory declarations not accepted: [declarationId1, declarationId2]",
  "instance": "/applications/APP123/esign"
}
```

---

# 4. Kafka Events

## Consumed

| Topic | Event | Action |
|-------|-------|--------|
| `pricing.final-decision` | `FinalDecisionApproved` | Create OfferAcceptanceSession |

## Published

| Topic | Event | Trigger |
|-------|-------|---------|
| `offer-acceptance.esign-completed` | `ESignCompleted` | E-sign successfully captured |

### ESignCompleted Payload

```json
{
  "applicationId": "APP123",
  "signedAt": "2026-06-30T10:15:00",
  "correlationId": "corr-abc-123"
}
```

---

# 5. HTTP Status Codes

| Code | Meaning |
|------|---------|
| 200 | Success |
| 404 | Session or resource not found |
| 409 | Conflict — already signed |
| 422 | Unprocessable — validation failure |
| 500 | Unexpected server error |
