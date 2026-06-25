# acquisition/invitation-to-apply/003-invitation-to-apply-api-spec.md

# Personal Loan Acquisition Platform

# Invitation To Apply API Specification

Version: 1.0

Status: Draft

Owner: Acquisition Domain

---

# 1. Purpose

This document defines REST API contracts for the Invitation To Apply capability.

The APIs enable:

* Invitation validation
* Offer retrieval
* Applicant prefill
* Application initiation

---

# 2. API Design Principles

## API-001 REST Based

Communication uses REST APIs.

---

## API-002 Resource Oriented

URLs represent business resources.

---

## API-003 Secure By Default

All APIs require:

* HTTPS
* Authentication
* Authorization
* Audit logging

---

## API-004 Standard Response Format

All responses follow enterprise API standards.

---

# 3. Base URL

Example:

```
https://api.company.com/acquisition/v1
```

---

# 4. Common Headers

Required:

| Header           | Description         |
| ---------------- | ------------------- |
| Authorization    | OAuth2 access token |
| X-Correlation-ID | End-to-end tracking |
| X-Client-ID      | Calling application |
| X-Request-ID     | Request identifier  |

---

Example:

```http
X-Correlation-ID: 8f91abc
```

---

# 5. API Endpoints Overview

| API                         | Purpose             |
| --------------------------- | ------------------- |
| POST /invitations/validate  | Validate invitation |
| GET /invitations/{id}/offer | Retrieve offer      |
| POST /applications/start    | Create application  |

---

# 6. Validate Invitation API

## Endpoint

```
POST /invitations/validate
```

---

## Purpose

Validate invitation identifier before starting application.

---

## Request

```json
{
  "invitationId": "INV-123456789"
}
```

---

# Request Model

## InvitationValidationRequest

| Field        | Type   | Required |
| ------------ | ------ | -------- |
| invitationId | String | Yes      |

---

# Validation Rules

Invitation ID:

* Cannot be null
* Cannot be empty
* Maximum length enforced

---

# Response

HTTP 200

```json
{
 "invitationId":"INV-123456789",
 "status":"VALID",
 "offerId":"OFF-98765",
 "expirationDate":"2026-12-31"
}
```

---

# Response Model

## InvitationValidationResponse

| Field          | Type   |
| -------------- | ------ |
| invitationId   | String |
| status         | String |
| offerId        | String |
| expirationDate | Date   |

---

# Possible Status Values

```text
VALID

INVALID

EXPIRED

USED
```

---

# Error Responses

## Invitation Not Found

HTTP 404

```json
{
 "error":{
   "code":"ITA-001",
   "message":"Invitation not found"
 }
}
```

---

## Invalid Format

HTTP 400

```json
{
 "error":{
   "code":"ITA-005",
   "message":"Invalid invitation format"
 }
}
```

---

# 7. Retrieve Offer API

## Endpoint

```
GET /invitations/{invitationId}/offer
```

---

# Purpose

Retrieve offer details associated with invitation.

---

# Path Parameters

| Parameter    | Type   |
| ------------ | ------ |
| invitationId | String |

---

# Response

HTTP 200

```json
{
 "offerId":"OFF-98765",
 "amount":{
    "min":5000,
    "max":25000
 },
 "apr":"12.5",
 "term":"60",
 "expirationDate":"2026-12-31"
}
```

---

# Offer Response Model

```json
{
 "offerId":"string",
 "amount":{},
 "apr":"number",
 "term":"number",
 "expirationDate":"date"
}
```

---

# Business Rules

Offer must be:

* Active
* Not expired
* Associated with invitation

---

# Errors

## Offer Expired

HTTP 422

```json
{
 "error":{
  "code":"ITA-002",
  "message":"Offer expired"
 }
}
```

---

## Offer Inactive

HTTP 422

```json
{
 "error":{
  "code":"ITA-003",
  "message":"Offer inactive"
 }
}
```

---

# 8. Start Application API

## Endpoint

```
POST /applications/start
```

---

# Purpose

Convert valid invitation into loan application.

---

# Request

```json
{
 "invitationId":"INV-123456789",
 "offerId":"OFF-98765"
}
```

---

# Request Model

## StartApplicationRequest

| Field        | Required |
| ------------ | -------- |
| invitationId | Yes      |
| offerId      | Yes      |

---

# Processing

System will:

1. Validate invitation
2. Retrieve offer
3. Create application
4. Associate invitation

---

# Response

HTTP 201

```json
{
 "applicationId":"APP-123456",
 "status":"STARTED",
 "applicant":{
   "firstName":"John",
   "lastName":"Smith",
   "address":{
      "city":"Phoenix",
      "state":"AZ"
   }
 }
}
```

---

# Response Model

## StartApplicationResponse

```json
{
 applicationId,
 status,
 applicant
}
```

---

# 9. Applicant Prefill API

## Endpoint

```
GET /invitations/{id}/applicant
```

---

# Purpose

Retrieve applicant information from Offer Management.

---

# Response

```json
{
 "firstName":"John",
 "lastName":"Smith",
 "address":{
   "line1":"123 Main St",
   "city":"Phoenix",
   "state":"AZ",
   "zip":"85001"
 }
}
```

---

# 10. External Offer Management Adapter API

This application consumes:

Enterprise Offer Management APIs.

---

# Outbound Call

Example:

```
GET /enterprise-offers/invitations/{invitationId}
```

---

# Request

```json
{
 "invitationId":"INV-12345"
}
```

---

# Response

```json
{
 "offerId":"OFF-111",
 "customer":{
   "firstName":"John",
   "lastName":"Smith"
 },
 "offerStatus":"ACTIVE"
}
```

---

# 11. Timeout Handling

Offer Management timeout:

Response:

HTTP 503

Error:

```json
{
 "error":{
  "code":"ITA-004",
  "message":"Offer service unavailable"
 }
}
```

---

# 12. Idempotency

Application creation APIs require:

Header:

```
Idempotency-Key
```

Example:

```http
Idempotency-Key: abc-123
```

---

Purpose:

Prevent duplicate applications.

---

# 13. Security

APIs require:

OAuth2 authentication

---

Authorization:

Roles:

```
PROSPECT

APPLICATION_USER

SYSTEM_CLIENT
```

---

# 14. Logging Requirements

Every API call logs:

* Correlation ID
* Request ID
* Application ID
* Response status
* Processing time

---

# 15. Metrics

Track:

* Invitation validations
* Success rate
* Expired offers
* Application creations
* API latency

---

# 16. API Versioning

Current:

```
/api/v1
```

Future breaking changes:

```
/api/v2
```

---

# 17. API Acceptance Criteria

☐ Invitation validation supported

☐ Offer retrieval supported

☐ Applicant prefill supported

☐ Application creation supported

☐ Error responses standardized

☐ Security enabled

☐ Audit logging enabled

---

# Related Documents

* 001-invitation-to-apply-spec.md
* 002-invitation-to-apply-state-machine.md
* 004-integration-spec.md
* 003-error-handling-standard.md
* 004-logging-standard.md
* OpenAPI YAML Specification
