package com.personalloan.applicationmanagement.domain.invitation;

import java.time.LocalDateTime;
import java.util.UUID;

public class ApplicationIntakeContext {

    private final UUID intakeId;
    private final UUID sessionId;
    private final ApplicationSource applicationSource;
    private final String invitationId;
    private final String offerId;
    private final String customerReferenceId;
    private final PrefillStatus prefillStatus;
    private final LocalDateTime createdTimestamp;

    private ApplicationIntakeContext(UUID intakeId, UUID sessionId, ApplicationSource applicationSource,
                                      String invitationId, String offerId, String customerReferenceId,
                                      PrefillStatus prefillStatus, LocalDateTime createdTimestamp) {
        this.intakeId = intakeId;
        this.sessionId = sessionId;
        this.applicationSource = applicationSource;
        this.invitationId = invitationId;
        this.offerId = offerId;
        this.customerReferenceId = customerReferenceId;
        this.prefillStatus = prefillStatus;
        this.createdTimestamp = createdTimestamp;
    }

    public static ApplicationIntakeContext createForInvitation(UUID sessionId, String invitationId,
                                                                String offerId, String customerReferenceId,
                                                                PrefillStatus prefillStatus) {
        return new ApplicationIntakeContext(
                UUID.randomUUID(), sessionId, ApplicationSource.INVITATION,
                invitationId, offerId, customerReferenceId, prefillStatus, LocalDateTime.now()
        );
    }

    public static ApplicationIntakeContext reconstitute(UUID intakeId, UUID sessionId, ApplicationSource applicationSource,
                                                         String invitationId, String offerId, String customerReferenceId,
                                                         PrefillStatus prefillStatus, LocalDateTime createdTimestamp) {
        return new ApplicationIntakeContext(intakeId, sessionId, applicationSource, invitationId,
                offerId, customerReferenceId, prefillStatus, createdTimestamp);
    }

    public UUID getIntakeId() { return intakeId; }
    public UUID getSessionId() { return sessionId; }
    public ApplicationSource getApplicationSource() { return applicationSource; }
    public String getInvitationId() { return invitationId; }
    public String getOfferId() { return offerId; }
    public String getCustomerReferenceId() { return customerReferenceId; }
    public PrefillStatus getPrefillStatus() { return prefillStatus; }
    public LocalDateTime getCreatedTimestamp() { return createdTimestamp; }
}
