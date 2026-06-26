package com.personalloan.applicationmanagement.application.pricing;

import com.personalloan.applicationmanagement.domain.application.Application;
import com.personalloan.applicationmanagement.domain.application.ApplicationStatus;
import com.personalloan.applicationmanagement.domain.application.port.ApplicationRepository;
import com.personalloan.applicationmanagement.domain.exception.ApplicationNotFoundException;
import com.personalloan.applicationmanagement.domain.pricing.ConsentCapturedEvent;
import com.personalloan.applicationmanagement.domain.pricing.ConsentRecord;
import com.personalloan.applicationmanagement.domain.pricing.port.ConsentRecordRepository;
import com.personalloan.applicationmanagement.domain.pricing.port.PricingEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CaptureConsentUseCase {

    private final ApplicationRepository applicationRepository;
    private final ConsentRecordRepository consentRecordRepository;
    private final PricingEventPublisher pricingEventPublisher;

    public CaptureConsentUseCase(ApplicationRepository applicationRepository,
                                  ConsentRecordRepository consentRecordRepository,
                                  PricingEventPublisher pricingEventPublisher) {
        this.applicationRepository = applicationRepository;
        this.consentRecordRepository = consentRecordRepository;
        this.pricingEventPublisher = pricingEventPublisher;
    }

    @Transactional
    public void execute(CaptureConsentCommand command) {
        Application application = applicationRepository.findByApplicationId(command.applicationId())
                .orElseThrow(() -> new ApplicationNotFoundException(command.applicationId()));

        ConsentRecord hardPullConsent = ConsentRecord.createHardPullConsent(
                command.applicationId(), command.consentChannel(), command.applicantReference());
        consentRecordRepository.save(hardPullConsent);

        ConsentRecord offerAcceptanceConsent = ConsentRecord.createOfferAcceptanceConsent(
                command.applicationId(), command.consentChannel(), command.selectedPricingOfferId());
        consentRecordRepository.save(offerAcceptanceConsent);

        application.transitionTo(ApplicationStatus.CONSENT_CAPTURED);
        applicationRepository.save(application);

        pricingEventPublisher.publishConsentCaptured(new ConsentCapturedEvent(
                command.applicationId(),
                command.selectedPricingOfferId(),
                command.applicantReference(),
                LocalDateTime.now()));
    }
}
