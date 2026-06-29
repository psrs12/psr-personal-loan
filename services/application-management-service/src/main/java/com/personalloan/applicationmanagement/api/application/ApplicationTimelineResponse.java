package com.personalloan.applicationmanagement.api.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ApplicationTimelineResponse(
        UUID applicationId,
        List<TimelineEvent> events
) {
    public record TimelineEvent(
            UUID auditId,
            String eventType,
            LocalDateTime eventTimestamp,
            UUID intakeId,
            String payload
    ) {}
}
