package com.personalloan.applicationmanagement.infrastructure.external.ssn.dto;

import java.time.LocalDateTime;

public record SSNVerifyResponse(
        String verificationToken,
        LocalDateTime expiresAt
) {}
