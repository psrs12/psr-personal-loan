package com.personalloan.offeracceptance.domain.offer;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public class ESignRecord {

    private final UUID eSignId;
    private final UUID applicationId;
    private final UUID sessionId;
    private final Set<UUID> acceptedDeclarationIds;
    private final String ipAddress;
    private final LocalDateTime signedAt;

    private ESignRecord(UUID eSignId, UUID applicationId, UUID sessionId,
                         Set<UUID> acceptedDeclarationIds, String ipAddress, LocalDateTime signedAt) {
        this.eSignId = eSignId;
        this.applicationId = applicationId;
        this.sessionId = sessionId;
        this.acceptedDeclarationIds = acceptedDeclarationIds;
        this.ipAddress = ipAddress;
        this.signedAt = signedAt;
    }

    public static ESignRecord create(UUID applicationId, UUID sessionId,
                                      Set<UUID> acceptedDeclarationIds, String ipAddress) {
        return new ESignRecord(UUID.randomUUID(), applicationId, sessionId,
                acceptedDeclarationIds, ipAddress, LocalDateTime.now());
    }

    public static ESignRecord reconstitute(UUID eSignId, UUID applicationId, UUID sessionId,
                                            Set<UUID> acceptedDeclarationIds, String ipAddress,
                                            LocalDateTime signedAt) {
        return new ESignRecord(eSignId, applicationId, sessionId, acceptedDeclarationIds, ipAddress, signedAt);
    }

    public UUID getESignId() { return eSignId; }
    public UUID getApplicationId() { return applicationId; }
    public UUID getSessionId() { return sessionId; }
    public Set<UUID> getAcceptedDeclarationIds() { return acceptedDeclarationIds; }
    public String getIpAddress() { return ipAddress; }
    public LocalDateTime getSignedAt() { return signedAt; }
}
