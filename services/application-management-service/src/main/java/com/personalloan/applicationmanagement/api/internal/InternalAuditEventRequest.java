package com.personalloan.applicationmanagement.api.internal;

public record InternalAuditEventRequest(
        String eventType,
        String payload
) {}
