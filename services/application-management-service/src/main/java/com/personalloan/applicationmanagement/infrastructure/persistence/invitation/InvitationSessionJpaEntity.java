package com.personalloan.applicationmanagement.infrastructure.persistence.invitation;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invitation_session")
public class InvitationSessionJpaEntity {

    @Id
    @Column(name = "session_id")
    private UUID sessionId;

    @Column(name = "invitation_id", nullable = false)
    private String invitationId;

    @Column(name = "application_source", nullable = false)
    private String applicationSource;

    @Column(name = "offer_id")
    private String offerId;

    @Column(name = "customer_reference_id")
    private String customerReferenceId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_timestamp", nullable = false)
    private LocalDateTime createdTimestamp;

    @Column(name = "updated_timestamp")
    private LocalDateTime updatedTimestamp;

    @Column(name = "expiration_timestamp", nullable = false)
    private LocalDateTime expirationTimestamp;

    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }
    public String getInvitationId() { return invitationId; }
    public void setInvitationId(String invitationId) { this.invitationId = invitationId; }
    public String getApplicationSource() { return applicationSource; }
    public void setApplicationSource(String applicationSource) { this.applicationSource = applicationSource; }
    public String getOfferId() { return offerId; }
    public void setOfferId(String offerId) { this.offerId = offerId; }
    public String getCustomerReferenceId() { return customerReferenceId; }
    public void setCustomerReferenceId(String customerReferenceId) { this.customerReferenceId = customerReferenceId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedTimestamp() { return createdTimestamp; }
    public void setCreatedTimestamp(LocalDateTime createdTimestamp) { this.createdTimestamp = createdTimestamp; }
    public LocalDateTime getUpdatedTimestamp() { return updatedTimestamp; }
    public void setUpdatedTimestamp(LocalDateTime updatedTimestamp) { this.updatedTimestamp = updatedTimestamp; }
    public LocalDateTime getExpirationTimestamp() { return expirationTimestamp; }
    public void setExpirationTimestamp(LocalDateTime expirationTimestamp) { this.expirationTimestamp = expirationTimestamp; }
}
