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

## POST

```
/ssn/verify
```

## Purpose

Verifies the applicant's SSN via an external SSN Verification Service before
application submission. Called after the prospect completes the form, before
POST /applications.

SSN is never logged. The verification token returned must be included in
the subsequent POST /applications request.

---

## Request

```json
{
  "ssn": "123-45-6789"
}
```

---

## Response

```json
{
  "verificationToken": "SSN-VRF-abc123xyz",

  "verified": true
}
```

---

## Failure Response

```json
{
  "errorCode": "SSN_VERIFICATION_FAILED",

  "message": "SSN could not be verified",

  "correlationId": "abc123"
}
```

---

# 3. Create Application

## POST

```
/applications
```

## Purpose

Creates a personal loan application. Called after the prospect has filled in
all required information and SSN has been verified.

For ITA path: include intakeId received from POST /invitations/initialize.
For DIRECT path: omit intakeId — applicationSource defaults to DIRECT.

---

## Request

```json
{
  "intakeId": "INT123",

  "ssnVerificationToken": "SSN-VRF-abc123xyz",

  "applicant": {

    "firstName": "John",

    "lastName": "Smith",

    "dateOfBirth": "1985-06-15",

    "citizenship": "US_CITIZEN",

    "address": {

      "street": "123 Main St",

      "city": "Phoenix",

      "state": "AZ",

      "zip": "85001"
    },

    "phone": "6025550100",

    "email": "john.smith@example.com",

    "employment": {

      "employerName": "Acme Corp",

      "employmentStatus": "EMPLOYED",

      "annualIncome": 75000
    }
  },

  "loanRequest": {

    "requestedAmount": 25000,

    "term": 60,

    "purpose": "DEBT_CONSOLIDATION"
  }
}
```

Note: `intakeId` is optional. Omit for DIRECT applications.

---

## Response

```json
{
  "applicationId": "APP123456",

  "status": "CREATED",

  "applicationSource": "INVITATION"
}
```

---

# 4. Retrieve Application

## GET

```
/applications/{applicationId}
```

---

# 5. Update Application

## PUT

```
/applications/{applicationId}
```

---

# 6. Submit Application

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
