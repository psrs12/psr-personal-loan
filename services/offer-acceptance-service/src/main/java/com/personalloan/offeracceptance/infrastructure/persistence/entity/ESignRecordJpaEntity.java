package com.personalloan.offeracceptance.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "esign_record")
public class ESignRecordJpaEntity {

    @Id
    @Column(name = "esign_id")
    private UUID eSignId;

    @Column(name = "application_id", nullable = false)
    private UUID applicationId;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "accepted_declaration_ids", nullable = false, columnDefinition = "TEXT")
    private String acceptedDeclarationIds;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "signed_at", nullable = false)
    private LocalDateTime signedAt;

    public UUID getESignId() { return eSignId; }
    public void setESignId(UUID eSignId) { this.eSignId = eSignId; }
    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }
    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }
    public String getAcceptedDeclarationIds() { return acceptedDeclarationIds; }
    public void setAcceptedDeclarationIds(String acceptedDeclarationIds) { this.acceptedDeclarationIds = acceptedDeclarationIds; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public LocalDateTime getSignedAt() { return signedAt; }
    public void setSignedAt(LocalDateTime signedAt) { this.signedAt = signedAt; }
}
