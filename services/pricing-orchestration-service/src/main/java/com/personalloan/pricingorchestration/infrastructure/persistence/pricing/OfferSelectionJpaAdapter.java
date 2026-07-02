package com.personalloan.pricingorchestration.infrastructure.persistence.pricing;

import com.personalloan.pricingorchestration.domain.pricing.OfferSelection;
import com.personalloan.pricingorchestration.domain.pricing.port.OfferSelectionRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class OfferSelectionJpaAdapter implements OfferSelectionRepository {

    private final OfferSelectionJpaRepository offerSelectionRepo;

    public OfferSelectionJpaAdapter(OfferSelectionJpaRepository offerSelectionRepo) {
        this.offerSelectionRepo = offerSelectionRepo;
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
}
