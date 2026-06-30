package com.personalloan.applicationmanagement.infrastructure.persistence.application;

import com.personalloan.applicationmanagement.domain.application.ApplicationOffer;
import com.personalloan.applicationmanagement.domain.application.port.ApplicationOfferRepository;
import org.springframework.stereotype.Component;

@Component
public class ApplicationOfferJpaAdapter implements ApplicationOfferRepository {

    private final ApplicationOfferJpaRepository applicationOfferRepo;

    public ApplicationOfferJpaAdapter(ApplicationOfferJpaRepository applicationOfferRepo) {
        this.applicationOfferRepo = applicationOfferRepo;
    }

    @Override
    public ApplicationOffer save(ApplicationOffer offer) {
        applicationOfferRepo.save(toEntity(offer));
        return offer;
    }

    private ApplicationOfferJpaEntity toEntity(ApplicationOffer o) {
        ApplicationOfferJpaEntity e = new ApplicationOfferJpaEntity();
        e.setApplicationOfferId(o.getApplicationOfferId());
        e.setApplicationId(o.getApplicationId());
        e.setOfferId(o.getOfferId());
        e.setCustomerReferenceId(o.getCustomerReferenceId());
        e.setLoanAmount(o.getLoanAmount());
        e.setApr(o.getApr());
        e.setTermMonths(o.getTermMonths());
        e.setExpirationDate(o.getExpirationDate());
        e.setCapturedTimestamp(o.getCapturedTimestamp());
        return e;
    }
}
