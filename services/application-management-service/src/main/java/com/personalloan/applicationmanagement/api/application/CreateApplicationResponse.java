package com.personalloan.applicationmanagement.api.application;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateApplicationResponse(
        UUID applicationId,
        String applicationStatus,
        String applicationSource,
        LocalDateTime createdTimestamp
) {}
