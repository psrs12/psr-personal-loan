package com.personalloan.offeracceptance.application.offer;

import com.personalloan.offeracceptance.domain.offer.OfferAcceptanceSession;
import com.personalloan.offeracceptance.domain.port.OfferAcceptanceSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CreateSessionUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateSessionUseCase.class);

    private final OfferAcceptanceSessionRepository sessionRepository;

    public CreateSessionUseCase(OfferAcceptanceSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public void execute(UUID applicationId) {
        if (sessionRepository.existsByApplicationId(applicationId)) {
            log.info("Session already exists for application {}, skipping (idempotent)", applicationId);
            return;
        }
        OfferAcceptanceSession session = OfferAcceptanceSession.create(applicationId);
        sessionRepository.save(session);
        log.info("Created offer acceptance session {} for application {}", session.getSessionId(), applicationId);
    }
}
