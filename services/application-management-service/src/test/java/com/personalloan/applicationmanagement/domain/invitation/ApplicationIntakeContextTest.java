package com.personalloan.applicationmanagement.domain.invitation;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationIntakeContextTest {

    @Test
    void createForInvitation_setsSourceToInvitation() {
        UUID sessionId = UUID.randomUUID();

        ApplicationIntakeContext context = ApplicationIntakeContext.createForInvitation(
                sessionId, "inv-001", "offer-123", "cref-456", PrefillStatus.COMPLETE
        );

        assertThat(context.getApplicationSource()).isEqualTo(ApplicationSource.INVITATION);
        assertThat(context.getSessionId()).isEqualTo(sessionId);
        assertThat(context.getInvitationId()).isEqualTo("inv-001");
        assertThat(context.getOfferId()).isEqualTo("offer-123");
        assertThat(context.getCustomerReferenceId()).isEqualTo("cref-456");
        assertThat(context.getPrefillStatus()).isEqualTo(PrefillStatus.COMPLETE);
        assertThat(context.getIntakeId()).isNotNull();
        assertThat(context.getCreatedTimestamp()).isNotNull();
    }

    @Test
    void createForInvitation_withPartialPrefill_setsPrefillStatusPartial() {
        ApplicationIntakeContext context = ApplicationIntakeContext.createForInvitation(
                UUID.randomUUID(), "inv-001", "offer-123", "cref-456", PrefillStatus.PARTIAL
        );

        assertThat(context.getPrefillStatus()).isEqualTo(PrefillStatus.PARTIAL);
    }
}
