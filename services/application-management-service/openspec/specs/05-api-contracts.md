# API Contracts

## Base Path

```
/api/v1/application-management
```

---

# 1. Initialize Invitation

## POST

```
/invitations/initialize
```

## Purpose

Validates invitation, retrieves offer and available customer information.
Returns prefill data and intake context identifier.

Does NOT create an Application. Application creation is a separate step
after the prospect completes the application form.

---

## Request

```json
{
  "invitationId": "ITA123456"
}
```

---

## Response

```json
{
  "intakeId": "INT123",

  "sessionId": "SESSION123",

  "prefillStatus": "COMPLETE",

  "offer": {

    "offerId": "OFFER123",

    "loanAmount": 25000,

    "apr": 10.99,

    "term": 60,

    "expirationDate": "2026-07-01"
  },

  "customer": {

    "firstName": "John",

    "lastName": "Smith",

    "address": {

       "street": "123 Main St",

       "city": "Phoenix",

       "state": "AZ",

       "zip": "85001"
    }
  }
}
```

Note: `customer` may be absent or partial when `prefillStatus` is `PARTIAL`.

---

# 2. Verify SSN

## POST `/ssn/verify`

## Purpose

Verifies the applicant's SSN via the external SSN Verification Service.
Must be called after the prospect completes the form and before `POST /applications`.

The returned `verificationToken` has a **15-minute TTL**. It must be included in
the subsequent `POST /applications` request together with the raw SSN for tokenisation.

SSN is **never logged**. The raw SSN must not appear in any log output.

---

## Request

```json
{
  "ssn": "123456789"
}
```

`ssn` — 9 digits, no dashes or spaces. Validated with `@Pattern(regexp = "\\d{9}")`.

---

## Response — 200 OK

```json
{
  "verificationToken": "SSN-VRF-abc123xyz",
  "verified": true
}
```

---

## Failure Response — 422

```json
{
  "type": "about:blank",
  "title": "Unprocessable Entity",
  "status": 422,
  "detail": "SSN could not be verified",
  "instance": "/api/v1/application-management/ssn/verify"
}
```

Error code: `SSN_VERIFICATION_FAILED`

---

# 3. Create Application

## POST `/applications`

## Purpose

Creates a personal loan application. Called after the prospect has completed the form
and SSN has been verified via `POST /ssn/verify`.

The `ssn` field is passed here for Bolt tokenisation. It is tokenised immediately and the
raw value is discarded — never persisted.

For ITA path: include `intakeId` from `POST /invitations/initialize`.
For DIRECT path: omit `intakeId`.

---

## Request

All fields are flat on the request record (no nested `applicant` or `loanRequest` objects).

```json
{
  "intakeId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "ssnVerificationToken": "SSN-VRF-abc123xyz",
  "ssn": "123456789",

  "firstName": "John",
  "lastName": "Smith",
  "dateOfBirth": "1985-06-15",
  "citizenship": "US_CITIZEN",

  "email": "john.smith@example.com",
  "phone": "6025550100",

  "street": "123 Main St",
  "city": "Phoenix",
  "state": "AZ",
  "zip": "85001",

  "employerName": "Acme Corp",
  "employmentStatus": "EMPLOYED",
  "annualIncome": 75000.00,

  "requestedAmount": 25000.00,
  "termMonths": 60,
  "loanPurpose": "DEBT_CONSOLIDATION"
}
```

### Field Validation

| Field | Required | Validation |
|-------|----------|-----------|
| `intakeId` | No | UUID; omit for DIRECT applications |
| `ssnVerificationToken` | Yes | Non-blank; must not be expired (15-min TTL) |
| `ssn` | Yes | 9 digits, no dashes (`\d{9}`) |
| `firstName` | Yes | Non-blank |
| `lastName` | Yes | Non-blank |
| `dateOfBirth` | Yes | ISO date (yyyy-MM-dd) |
| `citizenship` | Yes | `US_CITIZEN` \| `PERMANENT_RESIDENT` \| `DACA` \| `OTHER` |
| `email` | Yes | Non-blank |
| `phone` | Yes | Non-blank |
| `street` | Yes | Non-blank |
| `city` | Yes | Non-blank |
| `state` | Yes | Exactly 2 characters |
| `zip` | Yes | Non-blank |
| `employerName` | No | Optional; not required for `RETIRED` |
| `employmentStatus` | No | `EMPLOYED` \| `SELF_EMPLOYED` \| `RETIRED` \| `OTHER` |
| `annualIncome` | Yes | `>= 0.00` |
| `requestedAmount` | Yes | `>= 1000.00` |
| `termMonths` | Yes | 6 – 84 (inclusive) |
| `loanPurpose` | No | Free text |

### ITA Prefill Note

For ITA applications, `firstName`, `lastName`, `street`, `city`, `state`, and `zip`
are prefilled from the Customer Profile Platform and are read-only on the UI.
The applicant must still enter `citizenship`, `ssn`, `email`, `phone`, and all
employment / income fields regardless of channel.

---

## Response — 200 OK

```json
{
  "applicationId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "applicationStatus": "CREATED",
  "applicationSource": "INVITATION",
  "createdTimestamp": "2026-06-30T10:00:00"
}
```

---

# 4. Application Timeline

## GET `/applications/{applicationId}/timeline`

## Purpose

Returns the immutable event log for an application in chronological order.
Used by the applicant self-service portal to display the application journey.

Requires `Authorization: Bearer <token>` — protected endpoint.

---

## Response — 200 OK

```json
{
  "applicationId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "events": [
    {
      "auditId": "a1b2c3d4-...",
      "eventType": "APPLICATION_CREATED",
      "eventTimestamp": "2026-06-30T10:00:00",
      "intakeId": null,
      "payload": "{\"source\":\"DIRECT\",\"intakeId\":\"null\"}"
    }
  ]
}
```

Events are returned in ascending `eventTimestamp` order.

`intakeId` is nullable — present for INVITATION source applications only.

`payload` is a JSON string captured at audit record creation time.

---

## Failure Responses

| Status | Error Code | Condition |
|--------|------------|-----------|
| 401 | — | Missing or invalid `Authorization` header |
| 404 | APPLICATION_NOT_FOUND | `applicationId` does not exist |

---

# 5. Application Status

## GET `/applications/{applicationId}/status`

## Purpose

Returns the current status of an application. Used by the applicant self-service portal
to poll for status changes after login.

Requires `Authorization: Bearer <token>`.

---

## Response — 200 OK

```json
{
  "applicationId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "applicationStatus": "APPROVED"
}
```

## Failure Responses

| Status | Condition |
|--------|-----------|
| 401 | Missing or invalid `Authorization` header |
| 404 | `applicationId` does not exist |

---

# 6. Retrieve Application (stub)

## GET

```
/applications/{applicationId}
```

---

# 7. Update Application (stub)

## PUT

```
/applications/{applicationId}
```

---

# 8. Submit Application

## POST

```
/applications/{applicationId}/submit
```

---

# Error Response

```json
{
  "errorCode": "INVITATION_EXPIRED",

  "message": "Invitation has expired",

  "correlationId": "abc123"
}
```

---

# Error Codes

| Error Code                       | HTTP | Meaning                              |
| -------------------------------- | ---- | ------------------------------------ |
| INVITATION_NOT_FOUND             | 404  | Invitation identifier does not exist |
| INVITATION_EXPIRED               | 409  | Invitation has passed expiry date    |
| OFFER_UNAVAILABLE                | 503  | Offer Management Platform failure    |
| OFFER_EXPIRED                    | 409  | Offer is no longer active            |
| CUSTOMER_INFORMATION_UNAVAILABLE | 503  | Customer Profile Platform failure    |
| DUPLICATE_APPLICATION            | 409  | Active application already exists    |
| INTAKE_NOT_FOUND                 | 404  | Intake context does not exist        |
| INTAKE_EXPIRED                   | 409  | Intake context has expired           |
| SSN_VERIFICATION_FAILED          | 422  | SSN could not be verified            |
| SSN_VERIFICATION_TOKEN_INVALID   | 400  | SSN verification token missing or expired |

---

# HTTP Codes

| Code | Meaning                     |
| ---- | --------------------------- |
| 200  | Success                     |
| 201  | Created                     |
| 400  | Invalid Request             |
| 404  | Resource Not Found          |
| 409  | Invalid State               |
| 500  | System Error                |
| 503  | External Dependency Failure |

---

# Headers

Required:

```
Authorization: Bearer token

X-Correlation-ID

X-Channel-ID
```

X-Channel-ID values:

```
WEB

MOBILE

PARTNER
```
