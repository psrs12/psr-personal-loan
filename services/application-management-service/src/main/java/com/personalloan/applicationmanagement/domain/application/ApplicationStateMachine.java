package com.personalloan.applicationmanagement.domain.application;

import com.personalloan.applicationmanagement.domain.exception.InvalidStateTransitionException;

import java.util.Map;
import java.util.Set;

public class ApplicationStateMachine {

    private static final Map<ApplicationStatus, Set<ApplicationStatus>> ALLOWED_TRANSITIONS = Map.ofEntries(
            Map.entry(ApplicationStatus.CREATED, Set.of(ApplicationStatus.IN_PROGRESS, ApplicationStatus.CANCELLED)),
            Map.entry(ApplicationStatus.IN_PROGRESS, Set.of(ApplicationStatus.READY_FOR_SUBMISSION, ApplicationStatus.CANCELLED)),
            Map.entry(ApplicationStatus.READY_FOR_SUBMISSION, Set.of(ApplicationStatus.SUBMITTED, ApplicationStatus.IN_PROGRESS, ApplicationStatus.CANCELLED)),
            Map.entry(ApplicationStatus.SUBMITTED, Set.of(ApplicationStatus.SOFT_PULL_PENDING, ApplicationStatus.PROCESSING, ApplicationStatus.CANCELLED)),
            Map.entry(ApplicationStatus.PROCESSING, Set.of(ApplicationStatus.SOFT_PULL_PENDING, ApplicationStatus.APPROVED, ApplicationStatus.DECLINED, ApplicationStatus.REFERRED, ApplicationStatus.CANCELLED)),
            Map.entry(ApplicationStatus.SOFT_PULL_PENDING, Set.of(ApplicationStatus.PRICING_PENDING, ApplicationStatus.DECLINED, ApplicationStatus.EXPIRED)),
            Map.entry(ApplicationStatus.PRICING_PENDING, Set.of(ApplicationStatus.OFFER_PENDING, ApplicationStatus.DECLINED, ApplicationStatus.EXPIRED)),
            Map.entry(ApplicationStatus.OFFER_PENDING, Set.of(ApplicationStatus.CONSENT_CAPTURED, ApplicationStatus.EXPIRED)),
            Map.entry(ApplicationStatus.CONSENT_CAPTURED, Set.of(ApplicationStatus.HARD_PULL_PENDING, ApplicationStatus.EXPIRED)),
            Map.entry(ApplicationStatus.HARD_PULL_PENDING, Set.of(ApplicationStatus.DECISION_PENDING, ApplicationStatus.DECLINED, ApplicationStatus.EXPIRED)),
            Map.entry(ApplicationStatus.DECISION_PENDING, Set.of(ApplicationStatus.APPROVED, ApplicationStatus.DECLINED, ApplicationStatus.REFERRED, ApplicationStatus.DOCUMENTS_REQUIRED, ApplicationStatus.EXPIRED)),
            Map.entry(ApplicationStatus.REFERRED, Set.of(ApplicationStatus.APPROVED, ApplicationStatus.DECLINED, ApplicationStatus.DOCUMENTS_REQUIRED)),
            Map.entry(ApplicationStatus.APPROVED, Set.of(ApplicationStatus.OFFER_ACCEPTED)),
            Map.entry(ApplicationStatus.DOCUMENTS_REQUIRED, Set.of(ApplicationStatus.UNDERWRITING)),
            Map.entry(ApplicationStatus.OFFER_ACCEPTED, Set.of(ApplicationStatus.FUNDING_PENDING)),
            Map.entry(ApplicationStatus.UNDERWRITING, Set.of(ApplicationStatus.APPROVED, ApplicationStatus.DECLINED)),
            Map.entry(ApplicationStatus.FUNDING_PENDING, Set.of(ApplicationStatus.FUNDED)),
            Map.entry(ApplicationStatus.FUNDED, Set.of(ApplicationStatus.COMPLETED)),
            Map.entry(ApplicationStatus.COMPLETED, Set.of()),
            Map.entry(ApplicationStatus.DECLINED, Set.of()),
            Map.entry(ApplicationStatus.CANCELLED, Set.of()),
            Map.entry(ApplicationStatus.EXPIRED, Set.of())
    );

    public static void validate(ApplicationStatus from, ApplicationStatus to) {
        Set<ApplicationStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(from, Set.of());
        if (!allowed.contains(to)) {
            throw new InvalidStateTransitionException(from, to);
        }
    }
}
