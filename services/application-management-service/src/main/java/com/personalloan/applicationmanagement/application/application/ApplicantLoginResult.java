package com.personalloan.applicationmanagement.application.application;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApplicantLoginResult(
        String sessionToken,
        LocalDateTime expiresAt,
        UUID applicationId,
        String applicationStatus
) {}
