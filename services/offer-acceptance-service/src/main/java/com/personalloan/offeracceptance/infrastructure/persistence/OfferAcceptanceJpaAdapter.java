package com.personalloan.offeracceptance.infrastructure.persistence;

import com.personalloan.offeracceptance.domain.offer.*;
import com.personalloan.offeracceptance.domain.port.ESignRecordRepository;
import com.personalloan.offeracceptance.domain.port.OfferAcceptanceSessionRepository;
import com.personalloan.offeracceptance.infrastructure.persistence.entity.ESignRecordJpaEntity;
import com.personalloan.offeracceptance.infrastructure.persistence.entity.OfferAcceptanceSessionJpaEntity;
import com.personalloan.offeracceptance.infrastructure.persistence.repository.ESignRecordJpaRepository;
import com.personalloan.offeracceptance.infrastructure.persistence.repository.OfferAcceptanceSessionJpaRepository;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class OfferAcceptanceJpaAdapter implements OfferAcceptanceSessionRepository, ESignRecordRepository {

    private final OfferAcceptanceSessionJpaRepository sessionJpaRepo;
    private final ESignRecordJpaRepository eSignJpaRepo;

    public OfferAcceptanceJpaAdapter(OfferAcceptanceSessionJpaRepository sessionJpaRepo,
                                      ESignRecordJpaRepository eSignJpaRepo) {
        this.sessionJpaRepo = sessionJpaRepo;
        this.eSignJpaRepo = eSignJpaRepo;
    }

    @Override
    public void save(OfferAcceptanceSession session) {
        OfferAcceptanceSessionJpaEntity e = new OfferAcceptanceSessionJpaEntity();
        e.setSessionId(session.getSessionId());
        e.setApplicationId(session.getApplicationId());
        e.setStatus(session.getStatus().name());
        e.setCreatedAt(session.getCreatedAt());
        sessionJpaRepo.save(e);
    }

    @Override
    public Optional<OfferAcceptanceSession> findByApplicationId(UUID applicationId) {
        return sessionJpaRepo.findByApplicationId(applicationId)
                .map(e -> OfferAcceptanceSession.reconstitute(
                        e.getSessionId(),
                        e.getApplicationId(),
                        standardDeclarations(),
                        SessionStatus.valueOf(e.getStatus()),
                        e.getCreatedAt()
                ));
    }

    @Override
    public boolean existsByApplicationId(UUID applicationId) {
        return sessionJpaRepo.existsByApplicationId(applicationId);
    }

    @Override
    public void save(ESignRecord record) {
        ESignRecordJpaEntity e = new ESignRecordJpaEntity();
        e.setESignId(record.getESignId());
        e.setApplicationId(record.getApplicationId());
        e.setSessionId(record.getSessionId());
        e.setAcceptedDeclarationIds(record.getAcceptedDeclarationIds().stream()
                .map(UUID::toString).collect(Collectors.joining(",")));
        e.setIpAddress(record.getIpAddress());
        e.setSignedAt(record.getSignedAt());
        eSignJpaRepo.save(e);
    }

    private List<Declaration> standardDeclarations() {
        return OfferAcceptanceSession.STANDARD_DECLARATIONS;
    }
}
