package com.personalloan.pricingorchestration.application.pricing;

import com.personalloan.pricingorchestration.domain.pricing.ConsentRecord;
import com.personalloan.pricingorchestration.domain.pricing.event.ConsentCapturedEvent;
import com.personalloan.pricingorchestration.domain.pricing.port.ApplicationManagementPort;
import com.personalloan.pricingorchestration.domain.pricing.port.ConsentRecordRepository;
import com.personalloan.pricingorchestration.domain.pricing.port.PricingEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CaptureConsentUseCase {

    private final ConsentRecordRepository consentRecordRepository;
    private final PricingEventPublisher pricingEventPublisher;
    private final ApplicationManagementPort applicationManagementPort;

    public CaptureConsentUseCase(ConsentRecordRepository consentRecordRepository,
                                  PricingEventPublisher pricingEventPublisher,
                                  ApplicationManagementPort applicationManagementPort) {
        this.consentRecordRepository = consentRecordRepository;
        this.pricingEventPublisher = pricingEventPublisher;
        this.applicationManagementPort = applicationManagementPort;
    }

    @Transactional
    public void execute(CaptureConsentCommand command) {
        ConsentRecord hardPullConsent = ConsentRecord.createHardPullConsent(
                command.applicationId(), command.consentChannel(), command.applicantReference());
        consentRecordRepository.save(hardPullConsent);

        ConsentRecord offerAcceptanceConsent = ConsentRecord.createOfferAcceptanceConsent(
                command.applicationId(), command.consentChannel(), command.selectedPricingOfferId());
        consentRecordRepository.save(offerAcceptanceConsent);

        applicationManagementPort.updateApplicationStatus(command.applicationId(), "CONSENT_CAPTURED");

        applicationManagementPort.recordAuditEvent(
                command.applicationId(),
                "CONSENT_CAPTURED",
                "{\"selectedPricingOfferId\":\"" + command.selectedPricingOfferId() + "\",\"consentChannel\":\""
                        + command.consentChannel() + "\"}");

        pricingEventPublisher.publishConsentCaptured(new ConsentCapturedEvent(
                command.applicationId(),
                command.selectedPricingOfferId(),
                command.applicantReference(),
                LocalDateTime.now()));
    }
}
