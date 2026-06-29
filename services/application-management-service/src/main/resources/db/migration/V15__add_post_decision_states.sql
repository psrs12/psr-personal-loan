-- No schema change needed for application_status column — it is VARCHAR, not an enum type.
-- New status values DOCUMENTS_REQUIRED and OFFER_ACCEPTED are valid by application logic.
-- This migration documents the addition for audit purposes.

COMMENT ON COLUMN application.application_status IS
    'Valid values: CREATED, IN_PROGRESS, READY_FOR_SUBMISSION, SUBMITTED, PROCESSING, '
    'SOFT_PULL_PENDING, PRICING_PENDING, OFFER_PENDING, CONSENT_CAPTURED, HARD_PULL_PENDING, '
    'DECISION_PENDING, APPROVED, DECLINED, REFERRED, DOCUMENTS_REQUIRED, OFFER_ACCEPTED, '
    'CANCELLED, EXPIRED';
