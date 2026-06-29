package com.personalloan.applicationmanagement.infrastructure.persistence.pricing;

import com.personalloan.applicationmanagement.domain.pricing.*;
import com.personalloan.applicationmanagement.domain.pricing.port.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PricingJpaAdapter implements PricingOfferRepository, OfferSelectionRepository, ConsentRecordRepository {

    private final PricingOfferJpaRepository pricingOfferRepo;
    private final OfferSelectionJpaRepository offerSelectionRepo;
    private final ConsentRecordJpaRepository consentRecordRepo;

    public PricingJpaAdapter(PricingOfferJpaRepository pricingOfferRepo,
                              OfferSelectionJpaRepository offerSelectionRepo,
                              ConsentRecordJpaRepository consentRecordRepo) {
        this.pricingOfferRepo = pricingOfferRepo;
        this.offerSelectionRepo = offerSelectionRepo;
        this.consentRecordRepo = consentRecordRepo;
    }

    @Override
    public PricingOffer save(PricingOffer offer) {
        pricingOfferRepo.save(toEntity(offer));
        return offer;
    }

    @Override
    public List<PricingOffer> findByApplicationId(UUID applicationId) {
        return pricingOfferRepo.findByApplicationId(applicationId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<PricingOffer> findById(UUID pricingOfferId) {
        return pricingOfferRepo.findById(pricingOfferId).map(this::toDomain);
    }

    @Override
    public void saveAll(List<PricingOffer> offers) {
        pricingOfferRepo.saveAll(offers.stream().map(this::toEntity).toList());
    }

    @Override
    public OfferSelection save(OfferSelection selection) {
        offerSelectionRepo.save(toEntity(selection));
        return selection;
    }

    @Override
    public Optional<OfferSelection> findByApplicationId(UUID applicationId) {
        return offerSelectionRepo.findByApplicationId(applicationId).map(this::toDomain);
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

    private PricingOfferJpaEntity toEntity(PricingOffer o) {
        PricingOfferJpaEntity e = new PricingOfferJpaEntity();
        e.setPricingOfferId(o.getPricingOfferId());
        e.setApplicationId(o.getApplicationId());
        e.setApprovedAmount(o.getApprovedAmount());
        e.setInterestRate(o.getInterestRate());
        e.setApr(o.getApr());
        e.setTermMonths(o.getTermMonths());
        e.setMonthlyRepayment(o.getMonthlyRepayment());
        e.setTotalRepayable(o.getTotalRepayable());
        e.setOfferExpiryDate(o.getOfferExpiryDate());
        e.setPricingModelRef(o.getPricingModelRef());
        e.setBureauSnapshotRef(o.getBureauSnapshotRef());
        e.setOfferStatus(o.getOfferStatus().name());
        e.setCreatedAt(o.getCreatedAt());
        return e;
    }

    private PricingOffer toDomain(PricingOfferJpaEntity e) {
        return PricingOffer.reconstitute(e.getPricingOfferId(), e.getApplicationId(),
                e.getApprovedAmount(), e.getInterestRate(), e.getApr(), e.getTermMonths(),
                e.getMonthlyRepayment(), e.getTotalRepayable(), e.getOfferExpiryDate(),
                e.getPricingModelRef(), e.getBureauSnapshotRef(),
                PricingOfferStatus.valueOf(e.getOfferStatus()), e.getCreatedAt());
    }

    private OfferSelectionJpaEntity toEntity(OfferSelection s) {
        OfferSelectionJpaEntity e = new OfferSelectionJpaEntity();
        e.setId(s.getId());
        e.setApplicationId(s.getApplicationId());
        e.setSelectedPricingOfferId(s.getSelectedPricingOfferId());
        e.setOfferSelectedTimestamp(s.getOfferSelectedTimestamp());
        return e;
    }

    private OfferSelection toDomain(OfferSelectionJpaEntity e) {
        return OfferSelection.create(e.getApplicationId(), e.getSelectedPricingOfferId());
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
