package com.personalloan.pricingorchestration.application.pricing;

import com.personalloan.pricingorchestration.domain.pricing.ConsentRecord;
import com.personalloan.pricingorchestration.domain.pricing.ConsentType;
import com.personalloan.pricingorchestration.domain.pricing.event.ConsentCapturedEvent;
import com.personalloan.pricingorchestration.domain.pricing.port.ApplicationManagementPort;
import com.personalloan.pricingorchestration.domain.pricing.port.ConsentRecordRepository;
import com.personalloan.pricingorchestration.domain.pricing.port.PricingEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaptureConsentUseCaseTest {

    @Mock private ConsentRecordRepository consentRecordRepository;
    @Mock private PricingEventPublisher pricingEventPublisher;
    @Mock private ApplicationManagementPort applicationManagementPort;

    private CaptureConsentUseCase useCase;

    private final UUID applicationId = UUID.randomUUID();
    private final UUID pricingOfferId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new CaptureConsentUseCase(consentRecordRepository, pricingEventPublisher, applicationManagementPort);
    }

    @Test
    void execute_happyPath_savesConsentRecordsUpdatesStatusRecordsAuditAndPublishesEvent() {
        CaptureConsentCommand command = new CaptureConsentCommand(
                applicationId, pricingOfferId, "WEB", "applicant-ref-001");

        useCase.execute(command);

        ArgumentCaptor<ConsentRecord> captor = ArgumentCaptor.forClass(ConsentRecord.class);
        verify(consentRecordRepository, times(2)).save(captor.capture());
        var savedRecords = captor.getAllValues();
        assertThat(savedRecords).hasSize(2);
        assertThat(savedRecords.get(0).getConsentType()).isEqualTo(ConsentType.HARD_PULL);
        assertThat(savedRecords.get(0).getApplicantReference()).isEqualTo("applicant-ref-001");
        assertThat(savedRecords.get(1).getConsentType()).isEqualTo(ConsentType.OFFER_ACCEPTANCE);
        assertThat(savedRecords.get(1).getSelectedPricingOfferId()).isEqualTo(pricingOfferId);

        verify(applicationManagementPort).updateApplicationStatus(applicationId, "CONSENT_CAPTURED");
        verify(applicationManagementPort).recordAuditEvent(eq(applicationId), eq("CONSENT_CAPTURED"), anyString());

        ArgumentCaptor<ConsentCapturedEvent> eventCaptor = ArgumentCaptor.forClass(ConsentCapturedEvent.class);
        verify(pricingEventPublisher).publishConsentCaptured(eventCaptor.capture());
        ConsentCapturedEvent event = eventCaptor.getValue();
        assertThat(event.applicationId()).isEqualTo(applicationId);
        assertThat(event.selectedPricingOfferId()).isEqualTo(pricingOfferId);
        assertThat(event.applicantReference()).isEqualTo("applicant-ref-001");
        assertThat(event.consentCapturedAt()).isNotNull();
    }
}
