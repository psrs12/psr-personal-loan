package com.personalloan.applicationmanagement.infrastructure.persistence.application;

import com.personalloan.applicationmanagement.domain.application.*;
import com.personalloan.applicationmanagement.domain.application.port.ApplicationRepository;
import com.personalloan.applicationmanagement.domain.invitation.ApplicationSource;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ApplicationJpaAdapter implements ApplicationRepository {

    private final ApplicationJpaRepository applicationRepo;

    public ApplicationJpaAdapter(ApplicationJpaRepository applicationRepo) {
        this.applicationRepo = applicationRepo;
    }

    @Override
    public Application save(Application application) {
        applicationRepo.save(toEntity(application));
        return application;
    }

    @Override
    public Optional<Application> findByApplicationId(UUID applicationId) {
        return applicationRepo.findById(applicationId).map(this::toDomain);
    }

    @Override
    public boolean existsActiveApplicationForInvitation(String invitationId) {
        return applicationRepo.existsActiveApplicationForInvitation(invitationId);
    }

    private ApplicationJpaEntity toEntity(Application app) {
        ApplicationJpaEntity e = new ApplicationJpaEntity();
        e.setApplicationId(app.getApplicationId());
        e.setIntakeId(app.getIntakeId());
        e.setApplicationSource(app.getApplicationSource().name());
        e.setApplicationStatus(app.getApplicationStatus().name());
        e.setCreatedTimestamp(app.getCreatedTimestamp());
        e.setUpdatedTimestamp(app.getUpdatedTimestamp());
        e.setSoftPullCreditReportReferenceId(app.getSoftPullCreditReportReferenceId());
        e.setHardPullCreditReportReferenceId(app.getHardPullCreditReportReferenceId());
        e.setCampaignOfferId(app.getCampaignOfferId());
        e.setCampaignOfferTerms(app.getCampaignOfferTerms());
        e.setApplicationExpiryDate(app.getApplicationExpiryDate());
        return e;
    }

    private Application toDomain(ApplicationJpaEntity e) {
        return Application.reconstitute(
                e.getApplicationId(), e.getIntakeId(),
                ApplicationSource.valueOf(e.getApplicationSource()),
                ApplicationStatus.valueOf(e.getApplicationStatus()),
                e.getCreatedTimestamp(), e.getUpdatedTimestamp(),
                e.getSoftPullCreditReportReferenceId(),
                e.getHardPullCreditReportReferenceId(),
                e.getCampaignOfferId(),
                e.getCampaignOfferTerms(),
                e.getApplicationExpiryDate());
    }
}
