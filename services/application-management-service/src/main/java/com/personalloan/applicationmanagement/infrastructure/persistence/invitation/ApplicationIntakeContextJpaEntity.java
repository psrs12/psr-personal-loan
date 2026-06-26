package com.personalloan.applicationmanagement.infrastructure.persistence.invitation;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "application_intake_context")
public class ApplicationIntakeContextJpaEntity {

    @Id
    @Column(name = "intake_id")
    private UUID intakeId;

    @Column(name = "session_id")
    private UUID sessionId;

    @Column(name = "application_source", nullable = false)
    private String applicationSource;

    @Column(name = "invitation_id")
    private String invitationId;

    @Column(name = "offer_id")
    private String offerId;

    @Column(name = "customer_reference_id")
    private String customerReferenceId;

    @Column(name = "prefill_status", nullable = false)
    private String prefillStatus;

    @Column(name = "created_timestamp", nullable = false)
    private LocalDateTime createdTimestamp;

    public UUID getIntakeId() { return intakeId; }
    public void setIntakeId(UUID intakeId) { this.intakeId = intakeId; }
    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }
    public String getApplicationSource() { return applicationSource; }
    public void setApplicationSource(String applicationSource) { this.applicationSource = applicationSource; }
    public String getInvitationId() { return invitationId; }
    public void setInvitationId(String invitationId) { this.invitationId = invitationId; }
    public String getOfferId() { return offerId; }
    public void setOfferId(String offerId) { this.offerId = offerId; }
    public String getCustomerReferenceId() { return customerReferenceId; }
    public void setCustomerReferenceId(String customerReferenceId) { this.customerReferenceId = customerReferenceId; }
    public String getPrefillStatus() { return prefillStatus; }
    public void setPrefillStatus(String prefillStatus) { this.prefillStatus = prefillStatus; }
    public LocalDateTime getCreatedTimestamp() { return createdTimestamp; }
    public void setCreatedTimestamp(LocalDateTime createdTimestamp) { this.createdTimestamp = createdTimestamp; }
}
