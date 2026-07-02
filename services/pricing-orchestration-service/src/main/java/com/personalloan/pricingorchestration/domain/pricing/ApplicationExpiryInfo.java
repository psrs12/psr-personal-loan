package com.personalloan.pricingorchestration.domain.pricing;

import java.time.LocalDateTime;

public record ApplicationExpiryInfo(
        LocalDateTime applicationExpiryDate,
        String applicationStatus
) {
    public boolean isExpired() {
        return applicationExpiryDate != null && LocalDateTime.now().isAfter(applicationExpiryDate);
    }
}
