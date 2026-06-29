package com.personalloan.offeracceptance.application.offer;

import com.personalloan.offeracceptance.domain.exception.SessionNotFoundException;
import com.personalloan.offeracceptance.domain.offer.ESignRecord;
import com.personalloan.offeracceptance.domain.offer.OfferAcceptanceSession;
import com.personalloan.offeracceptance.domain.port.ESignRecordRepository;
import com.personalloan.offeracceptance.domain.port.OfferAcceptanceEventPublisher;
import com.personalloan.offeracceptance.domain.port.OfferAcceptanceSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
public class ESignUseCase {

    private final OfferAcceptanceSessionRepository sessionRepository;
    private final ESignRecordRepository eSignRecordRepository;
    private final OfferAcceptanceEventPublisher eventPublisher;

    public ESignUseCase(OfferAcceptanceSessionRepository sessionRepository,
                         ESignRecordRepository eSignRecordRepository,
                         OfferAcceptanceEventPublisher eventPublisher) {
        this.sessionRepository = sessionRepository;
        this.eSignRecordRepository = eSignRecordRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ESignRecord execute(UUID applicationId, Set<UUID> acceptedDeclarationIds, String ipAddress) {
        OfferAcceptanceSession session = sessionRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new SessionNotFoundException(applicationId));

        ESignRecord record = session.sign(acceptedDeclarationIds, ipAddress);

        sessionRepository.save(session);
        eSignRecordRepository.save(record);
        eventPublisher.publishESignCompleted(applicationId, record.getESignId(), record.getSignedAt());

        return record;
    }
}
