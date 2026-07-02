package com.personalloan.pricingorchestration.domain.pricing.port;

import com.personalloan.pricingorchestration.domain.pricing.ConsentRecord;
import com.personalloan.pricingorchestration.domain.pricing.ConsentType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConsentRecordRepository {
    ConsentRecord save(ConsentRecord record);
    List<ConsentRecord> findByApplicationId(UUID applicationId);
    Optional<ConsentRecord> findByApplicationIdAndConsentType(UUID applicationId, ConsentType consentType);
}
