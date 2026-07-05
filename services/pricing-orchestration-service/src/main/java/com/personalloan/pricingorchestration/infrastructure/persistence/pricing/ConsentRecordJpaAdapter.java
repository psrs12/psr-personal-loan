package com.personalloan.pricingorchestration.infrastructure.persistence.pricing;

import com.personalloan.pricingorchestration.domain.pricing.ConsentRecord;
import com.personalloan.pricingorchestration.domain.pricing.ConsentType;
import com.personalloan.pricingorchestration.domain.pricing.port.ConsentRecordRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ConsentRecordJpaAdapter implements ConsentRecordRepository {

    private final ConsentRecordJpaRepository consentRecordRepo;

    public ConsentRecordJpaAdapter(ConsentRecordJpaRepository consentRecordRepo) {
        this.consentRecordRepo = consentRecordRepo;
    }

    @Override
    public ConsentRecord save(ConsentRecord record) {
        consentRecordRepo.save(toEntity(record));
        return record;
    }

    @Override
    public List<ConsentRecord> findByApplicationId(UUID applicationId) {
        return consentRecordRepo.findByApplicationId(applicationId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<ConsentRecord> findByApplicationIdAndConsentType(UUID applicationId, ConsentType consentType) {
        return consentRecordRepo.findByApplicationIdAndConsentType(applicationId, consentType.name())
                .map(this::toDomain);
    }

    private ConsentRecordJpaEntity toEntity(ConsentRecord c) {
        ConsentRecordJpaEntity e = new ConsentRecordJpaEntity();
        e.setId(c.getId());
        e.setApplicationId(c.getApplicationId());
        e.setConsentType(c.getConsentType().name());
        e.setConsentGivenAt(c.getConsentGivenAt());
        e.setConsentChannel(c.getConsentChannel());
        e.setApplicantReference(c.getApplicantReference());
        e.setSelectedPricingOfferId(c.getSelectedPricingOfferId());
        return e;
    }

    private ConsentRecord toDomain(ConsentRecordJpaEntity e) {
        if (ConsentType.HARD_PULL.name().equals(e.getConsentType())) {
            return ConsentRecord.createHardPullConsent(e.getApplicationId(), e.getConsentChannel(), e.getApplicantReference());
        }
        return ConsentRecord.createOfferAcceptanceConsent(e.getApplicationId(), e.getConsentChannel(), e.getSelectedPricingOfferId());
    }
}
