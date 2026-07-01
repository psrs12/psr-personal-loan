package com.personalloan.applicationmanagement.application.application;

import com.personalloan.applicationmanagement.domain.application.*;
import com.personalloan.applicationmanagement.domain.application.Citizenship;
import com.personalloan.applicationmanagement.domain.application.port.*;
import com.personalloan.applicationmanagement.domain.exception.IntakeExpiredException;
import com.personalloan.applicationmanagement.domain.exception.IntakeNotFoundException;
import com.personalloan.applicationmanagement.domain.exception.SSNVerificationTokenInvalidException;
import com.personalloan.applicationmanagement.domain.invitation.ApplicationIntakeContext;
import com.personalloan.applicationmanagement.domain.invitation.ApplicationSource;
import com.personalloan.applicationmanagement.domain.invitation.InvitationSession;
import com.personalloan.applicationmanagement.domain.invitation.port.ApplicationIntakeContextRepository;
import com.personalloan.applicationmanagement.domain.invitation.port.InvitationSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CreateApplicationUseCase {

    private final ApplicationRepository applicationRepository;
    private final ApplicantRepository applicantRepository;
    private final LoanRequestRepository loanRequestRepository;
    private final ApplicationOfferRepository applicationOfferRepository;
    private final ApplicationAuditRepository applicationAuditRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ApplicationIntakeContextRepository applicationIntakeContextRepository;
    private final InvitationSessionRepository invitationSessionRepository;
    private final SSNTokenStore ssnTokenStore;
    private final BoltTokenizationPort boltTokenizationPort;

    public CreateApplicationUseCase(ApplicationRepository applicationRepository,
                                     ApplicantRepository applicantRepository,
                                     LoanRequestRepository loanRequestRepository,
                                     ApplicationOfferRepository applicationOfferRepository,
                                     ApplicationAuditRepository applicationAuditRepository,
                                     ApplicationEventPublisher applicationEventPublisher,
                                     ApplicationIntakeContextRepository applicationIntakeContextRepository,
                                     InvitationSessionRepository invitationSessionRepository,
                                     SSNTokenStore ssnTokenStore,
                                     BoltTokenizationPort boltTokenizationPort) {
        this.applicationRepository = applicationRepository;
        this.applicantRepository = applicantRepository;
        this.loanRequestRepository = loanRequestRepository;
        this.applicationOfferRepository = applicationOfferRepository;
        this.applicationAuditRepository = applicationAuditRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.applicationIntakeContextRepository = applicationIntakeContextRepository;
        this.invitationSessionRepository = invitationSessionRepository;
        this.ssnTokenStore = ssnTokenStore;
        this.boltTokenizationPort = boltTokenizationPort;
    }

    @Transactional
    public UUID execute(CreateApplicationCommand command) {
        Application application;

        if (command.intakeId() != null) {
            validateSsnToken(command.ssnVerificationToken());
            application = createFromITAIntake(command);
        } else {
            application = createDirect(command);
        }

        return application.getApplicationId();
    }

    private Application createFromITAIntake(CreateApplicationCommand command) {
        ApplicationIntakeContext intakeContext = applicationIntakeContextRepository
                .findByIntakeId(command.intakeId())
                .orElseThrow(() -> new IntakeNotFoundException(command.intakeId().toString()));

        InvitationSession session = invitationSessionRepository
                .findBySessionId(intakeContext.getSessionId())
                .orElseThrow(() -> new IntakeNotFoundException(command.intakeId().toString()));

        if (session.isExpired()) {
            throw new IntakeExpiredException(command.intakeId().toString());
        }

        Application application = Application.create(command.intakeId(), ApplicationSource.INVITATION);
        applicationRepository.save(application);

        persistApplicant(application.getApplicationId(), command);
        persistLoanRequest(application.getApplicationId(), command);
        persistApplicationOffer(application.getApplicationId(), intakeContext);
        persistAuditRecord(application.getApplicationId(), command.intakeId(), "INVITATION");
        publishEvent(application);

        return application;
    }

    private Application createDirect(CreateApplicationCommand command) {
        Application application = Application.createDirect();
        applicationRepository.save(application);

        persistApplicant(application.getApplicationId(), command);
        persistLoanRequest(application.getApplicationId(), command);
        persistAuditRecord(application.getApplicationId(), null, "DIRECT");
        publishEvent(application);

        return application;
    }

    private void validateSsnToken(String token) {
        if (token == null || !ssnTokenStore.isValid(token)) {
            throw new SSNVerificationTokenInvalidException();
        }
        ssnTokenStore.consume(token);
    }

    private void persistApplicant(UUID applicationId, CreateApplicationCommand command) {
        String rawSsn = command.ssn() != null ? command.ssn() : "";
        String ssnToken = boltTokenizationPort.tokenize(rawSsn);
        Citizenship citizenship = command.citizenship() != null ? command.citizenship() : Citizenship.US_CITIZEN;
        Applicant applicant = Applicant.create(
                applicationId, command.firstName(), command.lastName(), command.dateOfBirth(),
                citizenship, ssnToken, command.email(), command.phone(),
                command.street(), command.city(), command.state(), command.zip(),
                command.employerName(), command.employmentStatus(), command.annualIncome()
        );
        applicantRepository.save(applicant);
    }

    private void persistLoanRequest(UUID applicationId, CreateApplicationCommand command) {
        LoanRequest loanRequest = LoanRequest.create(applicationId, command.requestedAmount(),
                command.termMonths(), command.loanPurpose());
        loanRequestRepository.save(loanRequest);
    }

    private void persistApplicationOffer(UUID applicationId, ApplicationIntakeContext intakeContext) {
        ApplicationOffer offer = ApplicationOffer.capture(
                applicationId,
                intakeContext.getOfferId(),
                intakeContext.getCustomerReferenceId(),
                null, null, 0, null
        );
        applicationOfferRepository.save(offer);
    }

    private void persistAuditRecord(UUID applicationId, UUID intakeId, String source) {
        String payload = "{\"source\":\"" + source + "\",\"intakeId\":\"" + intakeId + "\"}";
        applicationAuditRepository.save(
                ApplicationAuditRecord.of(applicationId, intakeId, "APPLICATION_CREATED", payload)
        );
    }

    private void publishEvent(Application application) {
        applicationEventPublisher.publishApplicationCreated(
                new ApplicationCreatedEvent(application.getApplicationId(),
                        application.getApplicationSource(), application.getCreatedTimestamp())
        );
    }
}
