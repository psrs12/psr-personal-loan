package com.personalloan.offeracceptance.domain.port;

import java.time.LocalDateTime;
import java.util.UUID;

public interface OfferAcceptanceEventPublisher {
    void publishESignCompleted(UUID applicationId, UUID eSignId, LocalDateTime signedAt);
}
