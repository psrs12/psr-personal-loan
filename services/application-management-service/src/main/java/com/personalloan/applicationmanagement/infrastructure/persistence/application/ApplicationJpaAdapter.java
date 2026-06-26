package com.personalloan.applicationmanagement.infrastructure.persistence.application;

import com.personalloan.applicationmanagement.domain.application.*;
import com.personalloan.applicationmanagement.domain.application.port.*;
import com.personalloan.applicationmanagement.domain.invitation.ApplicationSource;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ApplicationJpaAdapter implements ApplicationRepository, ApplicantRepository,
        LoanRequestRepository, ApplicationOfferRepository, ApplicationAuditRepository {

    private final ApplicationJpaRepository applicationRepo;
    private final ApplicantJpaRepository applicantRepo;
    private final LoanRequestJpaRepository loanRequestRepo;
    private final ApplicationOfferJpaRepository applicationOfferRepo;
    private final ApplicationAuditJpaRepository auditRepo;

    public ApplicationJpaAdapter(ApplicationJpaRepository applicationRepo,
                                  ApplicantJpaRepository applicantRepo,
                                  LoanRequestJpaRepository loanRequestRepo,
                                  ApplicationOfferJpaRepository applicationOfferRepo,
                                  ApplicationAuditJpaRepository auditRepo) {
        this.applicationRepo = applicationRepo;
        this.applicantRepo = applicantRepo;
        this.loanRequestRepo = loanRequestRepo;
        this.applicationOfferRepo = applicationOfferRepo;
        this.auditRepo = auditRepo;
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

    @Override
    public Applicant save(Applicant applicant) {
        applicantRepo.save(toEntity(applicant));
        return applicant;
    }

    @Override
    public LoanRequest save(LoanRequest loanRequest) {
        loanRequestRepo.save(toEntity(loanRequest));
        return loanRequest;
    }

    @Override
    public ApplicationOffer save(ApplicationOffer offer) {
        applicationOfferRepo.save(toEntity(offer));
        return offer;
    }

    @Override
    public void save(ApplicationAuditRecord record) {
        ApplicationAuditJpaEntity entity = new ApplicationAuditJpaEntity();
        entity.setAuditId(record.auditId());
        entity.setApplicationId(record.applicationId());
        entity.setIntakeId(record.intakeId());
        entity.setEventType(record.eventType());
        entity.setEventTimestamp(record.eventTimestamp());
        entity.setPayload(record.payload());
        auditRepo.save(entity);
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

    private ApplicantJpaEntity toEntity(Applicant a) {
        ApplicantJpaEntity e = new ApplicantJpaEntity();
        e.setApplicantId(a.getApplicantId());
        e.setApplicationId(a.getApplicationId());
        e.setFirstName(a.getFirstName());
        e.setLastName(a.getLastName());
        e.setDateOfBirth(a.getDateOfBirth());
        e.setCitizenship(a.getCitizenship().name());
        e.setSsnToken(a.getSsnToken());
        e.setEmail(a.getEmail());
        e.setPhone(a.getPhone());
        e.setStreet(a.getStreet());
        e.setCity(a.getCity());
        e.setState(a.getState());
        e.setZip(a.getZip());
        e.setEmployerName(a.getEmployerName());
        e.setEmploymentStatus(a.getEmploymentStatus() != null ? a.getEmploymentStatus().name() : null);
        e.setAnnualIncome(a.getAnnualIncome());
        e.setCreatedTimestamp(a.getCreatedTimestamp());
        return e;
    }

    private LoanRequestJpaEntity toEntity(LoanRequest lr) {
        LoanRequestJpaEntity e = new LoanRequestJpaEntity();
        e.setLoanRequestId(lr.getLoanRequestId());
        e.setApplicationId(lr.getApplicationId());
        e.setRequestedAmount(lr.getRequestedAmount());
        e.setTermMonths(lr.getTermMonths());
        e.setLoanPurpose(lr.getLoanPurpose());
        e.setCreatedTimestamp(lr.getCreatedTimestamp());
        return e;
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
