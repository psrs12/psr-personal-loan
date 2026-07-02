package com.personalloan.pricingorchestration.infrastructure.external.applicationmanagement.dto;

public record AuditEventRequest(
        String eventType,
        String payload
) {}
