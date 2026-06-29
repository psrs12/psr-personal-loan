package com.personalloan.applicationmanagement.domain.application;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApplicationAuditRecord(
        UUID auditId,
        UUID applicationId,
        UUID intakeId,
        String eventType,
        LocalDateTime eventTimestamp,
        String payload
) {
    public static ApplicationAuditRecord of(UUID applicationId, UUID intakeId, String eventType, String payload) {
        return new ApplicationAuditRecord(UUID.randomUUID(), applicationId, intakeId, eventType, LocalDateTime.now(), payload);
    }
}
