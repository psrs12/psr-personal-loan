package com.personalloan.applicationmanagement.domain.invitation;

import java.time.LocalDateTime;
import java.util.UUID;

public class InvitationSession {

    private final UUID sessionId;
    private final String invitationId;
    private final ApplicationSource applicationSource;
    private String offerId;
    private String customerReferenceId;
    private InvitationSessionStatus status;
    private final LocalDateTime createdTimestamp;
    private LocalDateTime updatedTimestamp;
    private final LocalDateTime expirationTimestamp;

    private InvitationSession(UUID sessionId, String invitationId, ApplicationSource applicationSource,
                               LocalDateTime createdTimestamp, LocalDateTime expirationTimestamp) {
        this.sessionId = sessionId;
        this.invitationId = invitationId;
        this.applicationSource = applicationSource;
        this.status = InvitationSessionStatus.VALIDATED;
        this.createdTimestamp = createdTimestamp;
        this.expirationTimestamp = expirationTimestamp;
    }

    public static InvitationSession create(String invitationId, ApplicationSource source, int expirationMinutes) {
        LocalDateTime now = LocalDateTime.now();
        return new InvitationSession(
                UUID.randomUUID(),
                invitationId,
                source,
                now,
                now.plusMinutes(expirationMinutes)
        );
    }

    public static InvitationSession reconstitute(UUID sessionId, String invitationId, ApplicationSource applicationSource,
                                                  String offerId, String customerReferenceId,
                                                  InvitationSessionStatus status, LocalDateTime createdTimestamp,
                                                  LocalDateTime updatedTimestamp, LocalDateTime expirationTimestamp) {
        InvitationSession session = new InvitationSession(sessionId, invitationId, applicationSource, createdTimestamp, expirationTimestamp);
        session.offerId = offerId;
        session.customerReferenceId = customerReferenceId;
        session.status = status;
        session.updatedTimestamp = updatedTimestamp;
        return session;
    }

    public void offerRetrieved(String offerId, String customerReferenceId) {
        this.offerId = offerId;
        this.customerReferenceId = customerReferenceId;
        this.updatedTimestamp = LocalDateTime.now();
    }

    public void complete() {
        this.status = InvitationSessionStatus.COMPLETED;
        this.updatedTimestamp = LocalDateTime.now();
    }

    public void fail() {
        this.status = InvitationSessionStatus.FAILED;
        this.updatedTimestamp = LocalDateTime.now();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expirationTimestamp);
    }

    public UUID getSessionId() { return sessionId; }
    public String getInvitationId() { return invitationId; }
    public ApplicationSource getApplicationSource() { return applicationSource; }
    public String getOfferId() { return offerId; }
    public String getCustomerReferenceId() { return customerReferenceId; }
    public InvitationSessionStatus getStatus() { return status; }
    public LocalDateTime getCreatedTimestamp() { return createdTimestamp; }
    public LocalDateTime getUpdatedTimestamp() { return updatedTimestamp; }
    public LocalDateTime getExpirationTimestamp() { return expirationTimestamp; }
}
