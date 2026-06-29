package com.personalloan.pricingorchestration.domain.pricing.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApplicationCreatedEvent(
        UUID applicationId,
        String applicationSource,
        LocalDateTime createdTimestamp
) {}
