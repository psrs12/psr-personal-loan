package com.personalloan.applicationmanagement.domain.application;

import java.time.LocalDateTime;

public record SSNVerificationToken(
        String token,
        LocalDateTime expiresAt
) {
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
