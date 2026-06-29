package com.personalloan.applicationmanagement.domain.application;

import com.personalloan.applicationmanagement.domain.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

class ApplicationStateMachineTest {

    @ParameterizedTest(name = "{0} -> {1} is valid")
    @CsvSource({
            "SUBMITTED, SOFT_PULL_PENDING",
            "SOFT_PULL_PENDING, PRICING_PENDING",
            "SOFT_PULL_PENDING, DECLINED",
            "SOFT_PULL_PENDING, EXPIRED",
            "PRICING_PENDING, OFFER_PENDING",
            "PRICING_PENDING, DECLINED",
            "PRICING_PENDING, EXPIRED",
            "OFFER_PENDING, CONSENT_CAPTURED",
            "OFFER_PENDING, EXPIRED",
            "CONSENT_CAPTURED, HARD_PULL_PENDING",
            "CONSENT_CAPTURED, EXPIRED",
            "HARD_PULL_PENDING, DECISION_PENDING",
            "HARD_PULL_PENDING, DECLINED",
            "HARD_PULL_PENDING, EXPIRED",
            "DECISION_PENDING, APPROVED",
            "DECISION_PENDING, DECLINED",
            "DECISION_PENDING, REFERRED",
            "DECISION_PENDING, EXPIRED",
            "REFERRED, APPROVED",
            "REFERRED, DECLINED"
    })
    void validTransitions(String from, String to) {
        assertThatNoException().isThrownBy(() ->
                ApplicationStateMachine.validate(ApplicationStatus.valueOf(from), ApplicationStatus.valueOf(to)));
    }

    @ParameterizedTest(name = "{0} -> {1} is invalid")
    @CsvSource({
            "DECLINED, PROCESSING",
            "DECLINED, SOFT_PULL_PENDING",
            "EXPIRED, SOFT_PULL_PENDING",
            "APPROVED, SOFT_PULL_PENDING",
            "OFFER_PENDING, APPROVED",
            "CONSENT_CAPTURED, OFFER_PENDING",
            "HARD_PULL_PENDING, OFFER_PENDING",
            "DECISION_PENDING, SOFT_PULL_PENDING"
    })
    void invalidTransitions(String from, String to) {
        assertThatThrownBy(() ->
                ApplicationStateMachine.validate(ApplicationStatus.valueOf(from), ApplicationStatus.valueOf(to)))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void application_transitionTo_updatesStatus() {
        Application app = Application.create(null, com.personalloan.applicationmanagement.domain.invitation.ApplicationSource.DIRECT);
        app.transitionTo(ApplicationStatus.IN_PROGRESS);
        assertThat(app.getApplicationStatus()).isEqualTo(ApplicationStatus.IN_PROGRESS);
        assertThat(app.getUpdatedTimestamp()).isNotNull();
    }

    @Test
    void application_transitionTo_invalidTransition_throws() {
        Application app = Application.create(null, com.personalloan.applicationmanagement.domain.invitation.ApplicationSource.DIRECT);
        assertThatThrownBy(() -> app.transitionTo(ApplicationStatus.DECLINED))
                .isInstanceOf(InvalidStateTransitionException.class);
    }
}
