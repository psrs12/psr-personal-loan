package com.personalloan.applicationmanagement.infrastructure.persistence.pricing;

import com.personalloan.applicationmanagement.domain.pricing.*;
import com.personalloan.applicationmanagement.domain.pricing.port.PricingOfferRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PricingJpaAdapter implements PricingOfferRepository {

    private final PricingOfferJpaRepository pricingOfferRepo;

    public PricingJpaAdapter(PricingOfferJpaRepository pricingOfferRepo) {
        this.pricingOfferRepo = pricingOfferRepo;
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
}
