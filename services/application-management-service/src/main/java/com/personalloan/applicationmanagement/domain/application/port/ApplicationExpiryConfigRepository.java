package com.personalloan.applicationmanagement.domain.application.port;

import java.util.Optional;

public interface ApplicationExpiryConfigRepository {
    Optional<Integer> findExpiryThresholdDays(String productType, String channel);
}
