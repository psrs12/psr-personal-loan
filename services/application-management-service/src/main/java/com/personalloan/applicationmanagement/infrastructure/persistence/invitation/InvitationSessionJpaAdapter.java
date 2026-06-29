package com.personalloan.applicationmanagement.infrastructure.persistence.invitation;

import com.personalloan.applicationmanagement.domain.invitation.ApplicationSource;
import com.personalloan.applicationmanagement.domain.invitation.InvitationSession;
import com.personalloan.applicationmanagement.domain.invitation.InvitationSessionStatus;
import com.personalloan.applicationmanagement.domain.invitation.port.InvitationSessionRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class InvitationSessionJpaAdapter implements InvitationSessionRepository {

    private final InvitationSessionJpaRepository jpaRepository;

    public InvitationSessionJpaAdapter(InvitationSessionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public InvitationSession save(InvitationSession session) {
        InvitationSessionJpaEntity entity = toEntity(session);
        jpaRepository.save(entity);
        return session;
    }

    @Override
    public Optional<InvitationSession> findBySessionId(UUID sessionId) {
        return jpaRepository.findById(sessionId).map(this::toDomain);
    }

    private InvitationSessionJpaEntity toEntity(InvitationSession session) {
        InvitationSessionJpaEntity entity = new InvitationSessionJpaEntity();
        entity.setSessionId(session.getSessionId());
        entity.setInvitationId(session.getInvitationId());
        entity.setApplicationSource(session.getApplicationSource().name());
        entity.setOfferId(session.getOfferId());
        entity.setCustomerReferenceId(session.getCustomerReferenceId());
        entity.setStatus(session.getStatus().name());
        entity.setCreatedTimestamp(session.getCreatedTimestamp());
        entity.setUpdatedTimestamp(session.getUpdatedTimestamp());
        entity.setExpirationTimestamp(session.getExpirationTimestamp());
        return entity;
    }

    private InvitationSession toDomain(InvitationSessionJpaEntity entity) {
        return InvitationSession.reconstitute(
                entity.getSessionId(),
                entity.getInvitationId(),
                ApplicationSource.valueOf(entity.getApplicationSource()),
                entity.getOfferId(),
                entity.getCustomerReferenceId(),
                InvitationSessionStatus.valueOf(entity.getStatus()),
                entity.getCreatedTimestamp(),
                entity.getUpdatedTimestamp(),
                entity.getExpirationTimestamp()
        );
    }
}
