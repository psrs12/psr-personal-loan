package com.personalloan.applicationmanagement.domain.invitation;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InvitationSessionTest {

    @Test
    void create_setsStatusToValidated() {
        InvitationSession session = InvitationSession.create("inv-001", ApplicationSource.INVITATION, 30);

        assertThat(session.getStatus()).isEqualTo(InvitationSessionStatus.VALIDATED);
        assertThat(session.getSessionId()).isNotNull();
        assertThat(session.getInvitationId()).isEqualTo("inv-001");
    }

    @Test
    void create_setsExpirationTimestamp_thirtyMinutesFromNow() {
        LocalDateTime before = LocalDateTime.now();
        InvitationSession session = InvitationSession.create("inv-001", ApplicationSource.INVITATION, 30);
        LocalDateTime after = LocalDateTime.now();

        assertThat(session.getExpirationTimestamp()).isAfterOrEqualTo(before.plusMinutes(30));
        assertThat(session.getExpirationTimestamp()).isBeforeOrEqualTo(after.plusMinutes(30));
    }

    @Test
    void isExpired_returnsFalse_whenWithinExpiration() {
        InvitationSession session = InvitationSession.create("inv-001", ApplicationSource.INVITATION, 30);

        assertThat(session.isExpired()).isFalse();
    }

    @Test
    void isExpired_returnsTrue_whenPastExpiration() {
        InvitationSession session = InvitationSession.reconstitute(
                UUID.randomUUID(), "inv-001", ApplicationSource.INVITATION,
                null, null, InvitationSessionStatus.VALIDATED,
                LocalDateTime.now().minusHours(1), null,
                LocalDateTime.now().minusMinutes(1)
        );

        assertThat(session.isExpired()).isTrue();
    }

    @Test
    void complete_setsStatusToCompleted() {
        InvitationSession session = InvitationSession.create("inv-001", ApplicationSource.INVITATION, 30);

        session.complete();

        assertThat(session.getStatus()).isEqualTo(InvitationSessionStatus.COMPLETED);
        assertThat(session.getUpdatedTimestamp()).isNotNull();
    }

    @Test
    void fail_setsStatusToFailed() {
        InvitationSession session = InvitationSession.create("inv-001", ApplicationSource.INVITATION, 30);

        session.fail();

        assertThat(session.getStatus()).isEqualTo(InvitationSessionStatus.FAILED);
    }

    @Test
    void offerRetrieved_setsOfferIdAndCustomerReferenceId() {
        InvitationSession session = InvitationSession.create("inv-001", ApplicationSource.INVITATION, 30);

        session.offerRetrieved("offer-123", "cref-456");

        assertThat(session.getOfferId()).isEqualTo("offer-123");
        assertThat(session.getCustomerReferenceId()).isEqualTo("cref-456");
        assertThat(session.getUpdatedTimestamp()).isNotNull();
    }
}
