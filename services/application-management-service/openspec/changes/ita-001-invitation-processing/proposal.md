## Why

Prospects who receive a Personal Loan marketing invitation need a streamlined entry point to begin their application. Without a validated ITA intake capability, the platform cannot support pre-approved offer journeys or reduce applicant data entry for existing customers.

## What Changes

- Introduce the Invitation To Apply (ITA) intake endpoint (`POST /invitations/initialize`) that validates an invitation, retrieves offer details, retrieves customer prefill data (name and address only), and returns an intake context with prefill information.
- Introduce the Application creation endpoint (`POST /applications`) that accepts the intake context identifier and applicant-provided information to create a Personal Loan application.
- Application creation is a separate step from invitation processing — invitation processing produces prefill data only; the applicant completes the form before creating the application.
- Persist InvitationSession, ApplicationIntakeContext, offer snapshot (intake time), Application, Applicant, LoanRequest, and ApplicationOffer (application time) to PostgreSQL.
- Publish domain events: ApplicationCreated.
- Enforce business rules: invitation validity, offer availability, invitation reuse, session expiration (30 minutes), and duplicate application prevention.

## Capabilities

### New Capabilities

- `invitation-processing`: Validate invitation, retrieve offer and customer prefill data, create InvitationSession and ApplicationIntakeContext, return prefill response.
- `application-creation`: Create a Personal Loan application from an intake context with applicant-provided and prefilled information. Persist ApplicationOffer snapshot at creation time.

### Modified Capabilities

*(none — this is the initial implementation of the Application Management service)*

## Impact

- **New service**: `application-management-service` (Spring Boot, Java 21)
- **New APIs**: `POST /api/v1/application-management/invitations/initialize`, `POST /api/v1/application-management/applications`
- **External dependencies**: Enterprise Offer Management Platform (synchronous), Customer Profile Platform (synchronous)
- **Database**: PostgreSQL — new schema with Flyway migrations
- **Events**: ApplicationCreated published to Kafka
- **Security**: All endpoints require Bearer token authentication and X-Correlation-ID / X-Channel-ID headers
