package com.personalloan.applicationmanagement.api.application;

import java.util.UUID;

public record ApplicationStatusResponse(
        UUID applicationId,
        String applicationStatus
) {}
