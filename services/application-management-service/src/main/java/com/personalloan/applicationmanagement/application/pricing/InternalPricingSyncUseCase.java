package com.personalloan.applicationmanagement.application.pricing;

import com.personalloan.applicationmanagement.api.internal.InternalPricingDataResponse;
import com.personalloan.applicationmanagement.domain.application.Applicant;
import com.personalloan.applicationmanagement.domain.application.Application;
import com.personalloan.applicationmanagement.domain.application.ApplicationAuditRecord;
import com.personalloan.applicationmanagement.domain.application.ApplicationStatus;
import com.personalloan.applicationmanagement.domain.application.LoanRequest;
import com.personalloan.applicationmanagement.domain.application.port.ApplicantRepository;
import com.personalloan.applicationmanagement.domain.application.port.ApplicationAuditRepository;
import com.personalloan.applicationmanagement.domain.application.port.ApplicationRepository;
import com.personalloan.applicationmanagement.domain.application.port.LoanRequestRepository;
import com.personalloan.applicationmanagement.domain.exception.ApplicationNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class InternalPricingSyncUseCase {

    private final ApplicationRepository applicationRepository;
    private final ApplicantRepository applicantRepository;
    private final LoanRequestRepository loanRequestRepository;
    private final ApplicationAuditRepository applicationAuditRepository;

    public InternalPricingSyncUseCase(ApplicationRepository applicationRepository,
                                       ApplicantRepository applicantRepository,
                                       LoanRequestRepository loanRequestRepository,
                                       ApplicationAuditRepository applicationAuditRepository) {
        this.applicationRepository = applicationRepository;
        this.applicantRepository = applicantRepository;
        this.loanRequestRepository = loanRequestRepository;
        this.applicationAuditRepository = applicationAuditRepository;
    }

    @Transactional(readOnly = true)
    public InternalPricingDataResponse getPricingData(UUID applicationId) {
        Application application = findApplication(applicationId);
        Applicant applicant = applicantRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException(applicationId));
        LoanRequest loanRequest = loanRequestRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException(applicationId));

        return new InternalPricingDataResponse(
                application.getSoftPullCreditReportReferenceId(),
                loanRequest.getRequestedAmount(),
                loanRequest.getTermMonths(),
                loanRequest.getLoanPurpose(),
                applicant.getAnnualIncome(),
                applicant.getEmploymentStatus() != null ? applicant.getEmploymentStatus().name() : null,
                application.getCampaignOfferId(),
                application.getCampaignOfferTerms(),
                application.getApplicationExpiryDate(),
                application.getApplicationStatus() != null ? application.getApplicationStatus().name() : null
        );
    }

    @Transactional
    public void recordAuditEvent(UUID applicationId, String eventType, String payload) {
        Application application = findApplication(applicationId);
        applicationAuditRepository.save(
                ApplicationAuditRecord.of(applicationId, application.getIntakeId(), eventType, payload));
    }

    @Transactional
    public void updateStatus(UUID applicationId, String status) {
        Application application = findApplication(applicationId);
        application.transitionTo(ApplicationStatus.valueOf(status));
        applicationRepository.save(application);
    }

    @Transactional
    public void persistSoftPullReference(UUID applicationId, String referenceId) {
        Application application = findApplication(applicationId);
        application.applySoftPullReference(referenceId);
        applicationRepository.save(application);
    }

    @Transactional
    public void persistHardPullReference(UUID applicationId, String referenceId) {
        Application application = findApplication(applicationId);
        application.applyHardPullReference(referenceId);
        applicationRepository.save(application);
    }

    private Application findApplication(UUID applicationId) {
        return applicationRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException(applicationId));
    }
}
