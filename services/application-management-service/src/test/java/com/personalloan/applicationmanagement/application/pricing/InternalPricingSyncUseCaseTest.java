package com.personalloan.applicationmanagement.application.pricing;

import com.personalloan.applicationmanagement.api.internal.InternalPricingDataResponse;
import com.personalloan.applicationmanagement.domain.application.Applicant;
import com.personalloan.applicationmanagement.domain.application.Application;
import com.personalloan.applicationmanagement.domain.application.ApplicationAuditRecord;
import com.personalloan.applicationmanagement.domain.application.ApplicationStatus;
import com.personalloan.applicationmanagement.domain.application.Citizenship;
import com.personalloan.applicationmanagement.domain.application.EmploymentStatus;
import com.personalloan.applicationmanagement.domain.application.LoanRequest;
import com.personalloan.applicationmanagement.domain.application.port.ApplicantRepository;
import com.personalloan.applicationmanagement.domain.application.port.ApplicationAuditRepository;
import com.personalloan.applicationmanagement.domain.application.port.ApplicationRepository;
import com.personalloan.applicationmanagement.domain.application.port.LoanRequestRepository;
import com.personalloan.applicationmanagement.domain.exception.ApplicationNotFoundException;
import com.personalloan.applicationmanagement.domain.invitation.ApplicationSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InternalPricingSyncUseCaseTest {

    @Mock private ApplicationRepository applicationRepository;
    @Mock private ApplicantRepository applicantRepository;
    @Mock private LoanRequestRepository loanRequestRepository;
    @Mock private ApplicationAuditRepository applicationAuditRepository;

    private InternalPricingSyncUseCase useCase;

    private final UUID applicationId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new InternalPricingSyncUseCase(applicationRepository, applicantRepository,
                loanRequestRepository, applicationAuditRepository);
    }

    private Application directApplication() {
        return Application.createDirect();
    }

    @Test
    void recordAuditEvent_happyPath_savesAuditRecordWithApplicationAndIntakeId() {
        Application application = Application.create(UUID.randomUUID(), ApplicationSource.INVITATION);
        when(applicationRepository.findByApplicationId(applicationId)).thenReturn(Optional.of(application));

        useCase.recordAuditEvent(applicationId, "PRICING_OFFERS_RECEIVED", "{\"foo\":\"bar\"}");

        ArgumentCaptor<ApplicationAuditRecord> captor = ArgumentCaptor.forClass(ApplicationAuditRecord.class);
        verify(applicationAuditRepository).save(captor.capture());
        ApplicationAuditRecord saved = captor.getValue();
        assertThat(saved.applicationId()).isEqualTo(applicationId);
        assertThat(saved.intakeId()).isEqualTo(application.getIntakeId());
        assertThat(saved.eventType()).isEqualTo("PRICING_OFFERS_RECEIVED");
        assertThat(saved.payload()).isEqualTo("{\"foo\":\"bar\"}");
    }

    @Test
    void recordAuditEvent_applicationNotFound_throwsApplicationNotFoundException() {
        when(applicationRepository.findByApplicationId(applicationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.recordAuditEvent(applicationId, "PRICING_OFFERS_RECEIVED", "{}"))
                .isInstanceOf(ApplicationNotFoundException.class);

        verifyNoInteractions(applicationAuditRepository);
    }

    @Test
    void getPricingData_returnsExpiryDateAndStatusPopulated() {
        Application application = directApplication();
        application.applyExpiryThreshold(30);
        application.applySoftPullReference("soft-ref-123");

        Applicant applicant = Applicant.create(applicationId, "Jane", "Doe", LocalDate.of(1990, 1, 1),
                Citizenship.US_CITIZEN, "ssn-token", "jane@example.com", "555-1234",
                "123 Main St", null, "Springfield", "IL", "62701",
                "Acme Corp", EmploymentStatus.EMPLOYED, new BigDecimal("75000"));

        LoanRequest loanRequest = LoanRequest.create(applicationId, new BigDecimal("10000"), 36, "HOME_IMPROVEMENT");

        when(applicationRepository.findByApplicationId(applicationId)).thenReturn(Optional.of(application));
        when(applicantRepository.findByApplicationId(applicationId)).thenReturn(Optional.of(applicant));
        when(loanRequestRepository.findByApplicationId(applicationId)).thenReturn(Optional.of(loanRequest));

        InternalPricingDataResponse response = useCase.getPricingData(applicationId);

        assertThat(response.softPullCreditReportReferenceId()).isEqualTo("soft-ref-123");
        assertThat(response.requestedAmount()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(response.requestedTermMonths()).isEqualTo(36);
        assertThat(response.loanPurpose()).isEqualTo("HOME_IMPROVEMENT");
        assertThat(response.annualIncome()).isEqualByComparingTo(new BigDecimal("75000"));
        assertThat(response.employmentStatus()).isEqualTo("EMPLOYED");
        assertThat(response.applicationExpiryDate()).isEqualTo(application.getApplicationExpiryDate());
        assertThat(response.applicationExpiryDate()).isNotNull();
        assertThat(response.applicationStatus()).isEqualTo(application.getApplicationStatus().name());
        assertThat(response.applicationStatus()).isEqualTo(ApplicationStatus.CREATED.name());
    }
}
