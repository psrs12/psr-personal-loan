package com.personalloan.applicationmanagement.api.ssn;

import java.time.LocalDateTime;

public record SSNVerifyResponse(
        String verificationToken,
        LocalDateTime expiresAt
) {}
