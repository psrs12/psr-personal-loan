package com.personalloan.applicationmanagement.application.invitation;

import com.personalloan.applicationmanagement.domain.application.ApplicationAuditRecord;
import com.personalloan.applicationmanagement.domain.application.port.ApplicationAuditRepository;
import com.personalloan.applicationmanagement.domain.application.port.ApplicationRepository;
import com.personalloan.applicationmanagement.domain.exception.DuplicateApplicationException;
import com.personalloan.applicationmanagement.domain.exception.OfferExpiredException;
import com.personalloan.applicationmanagement.domain.invitation.*;
import com.personalloan.applicationmanagement.domain.invitation.port.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class ProcessInvitationUseCase {

    private final OfferManagementPort offerManagementPort;
    private final CustomerProfilePort customerProfilePort;
    private final InvitationSessionRepository invitationSessionRepository;
    private final ApplicationIntakeContextRepository applicationIntakeContextRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationAuditRepository applicationAuditRepository;

    @Value("${session.expiration-minutes:30}")
    private int sessionExpirationMinutes;

    public ProcessInvitationUseCase(OfferManagementPort offerManagementPort,
                                     CustomerProfilePort customerProfilePort,
                                     InvitationSessionRepository invitationSessionRepository,
                                     ApplicationIntakeContextRepository applicationIntakeContextRepository,
                                     ApplicationRepository applicationRepository,
                                     ApplicationAuditRepository applicationAuditRepository) {
        this.offerManagementPort = offerManagementPort;
        this.customerProfilePort = customerProfilePort;
        this.invitationSessionRepository = invitationSessionRepository;
        this.applicationIntakeContextRepository = applicationIntakeContextRepository;
        this.applicationRepository = applicationRepository;
        this.applicationAuditRepository = applicationAuditRepository;
    }

    @Transactional
    public ProcessInvitationResult execute(String invitationId) {
        if (applicationRepository.existsActiveApplicationForInvitation(invitationId)) {
            throw new DuplicateApplicationException(invitationId);
        }

        offerManagementPort.validateInvitation(invitationId);

        InvitationSession session = InvitationSession.create(invitationId, ApplicationSource.INVITATION, sessionExpirationMinutes);
        invitationSessionRepository.save(session);

        OfferDetails offerDetails = offerManagementPort.retrieveOffer(invitationId);

        if (offerDetails.expirationDate() != null && offerDetails.expirationDate().isBefore(LocalDate.now())) {
            session.fail();
            invitationSessionRepository.save(session);
            throw new OfferExpiredException(offerDetails.offerId());
        }

        session.offerRetrieved(offerDetails.offerId(), offerDetails.customerReferenceId());
        invitationSessionRepository.save(session);

        Optional<CustomerPrefill> customerPrefill = tryRetrieveCustomer(offerDetails.customerReferenceId());

        PrefillStatus prefillStatus = customerPrefill.isPresent() ? PrefillStatus.COMPLETE : PrefillStatus.PARTIAL;

        ApplicationIntakeContext intakeContext = ApplicationIntakeContext.createForInvitation(
                session.getSessionId(),
                invitationId,
                offerDetails.offerId(),
                offerDetails.customerReferenceId(),
                prefillStatus
        );
        applicationIntakeContextRepository.save(intakeContext);

        session.complete();
        invitationSessionRepository.save(session);

        applicationAuditRepository.save(ApplicationAuditRecord.of(
                null, intakeContext.getIntakeId(),
                "INVITATION_PROCESSED",
                "{\"invitationId\":\"" + invitationId + "\",\"offerId\":\"" + offerDetails.offerId() +
                "\",\"prefillStatus\":\"" + prefillStatus.name() + "\"}"
        ));

        return new ProcessInvitationResult(intakeContext.getIntakeId(), session.getSessionId(),
                offerDetails, customerPrefill.orElse(null), prefillStatus);
    }

    private Optional<CustomerPrefill> tryRetrieveCustomer(String customerReferenceId) {
        try {
            return customerProfilePort.retrieveCustomer(customerReferenceId);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
