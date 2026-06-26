package com.personalloan.applicationmanagement.domain.application;

public enum ApplicationStatus {
    CREATED,
    IN_PROGRESS,
    READY_FOR_SUBMISSION,
    SUBMITTED,
    PROCESSING,
    SOFT_PULL_PENDING,
    PRICING_PENDING,
    OFFER_PENDING,
    CONSENT_CAPTURED,
    HARD_PULL_PENDING,
    DECISION_PENDING,
    APPROVED,
    DECLINED,
    REFERRED,
    CANCELLED,
    EXPIRED
}
