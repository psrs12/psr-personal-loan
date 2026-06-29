package com.personalloan.applicationmanagement.api.application;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApplicantLoginResponse(
        String sessionToken,
        LocalDateTime expiresAt,
        UUID applicationId,
        String applicationStatus
) {}
