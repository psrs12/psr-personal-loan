package com.personalloan.pricingorchestration.domain.pricing.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record SoftPullCompletedEvent(
        UUID applicationId,
        String creditReportReferenceId,
        LocalDateTime completedAt
) {}
