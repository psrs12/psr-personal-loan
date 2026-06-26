package com.personalloan.applicationmanagement.infrastructure.persistence.invitation;

import com.personalloan.applicationmanagement.domain.invitation.ApplicationIntakeContext;
import com.personalloan.applicationmanagement.domain.invitation.ApplicationSource;
import com.personalloan.applicationmanagement.domain.invitation.PrefillStatus;
import com.personalloan.applicationmanagement.domain.invitation.port.ApplicationIntakeContextRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ApplicationIntakeContextJpaAdapter implements ApplicationIntakeContextRepository {

    private final ApplicationIntakeContextJpaRepository jpaRepository;

    public ApplicationIntakeContextJpaAdapter(ApplicationIntakeContextJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ApplicationIntakeContext save(ApplicationIntakeContext context) {
        jpaRepository.save(toEntity(context));
        return context;
    }

    @Override
    public Optional<ApplicationIntakeContext> findByIntakeId(UUID intakeId) {
        return jpaRepository.findById(intakeId).map(this::toDomain);
    }

    @Override
    public Optional<ApplicationIntakeContext> findByInvitationId(String invitationId) {
        return jpaRepository.findByInvitationId(invitationId).map(this::toDomain);
    }

    private ApplicationIntakeContextJpaEntity toEntity(ApplicationIntakeContext context) {
        ApplicationIntakeContextJpaEntity entity = new ApplicationIntakeContextJpaEntity();
        entity.setIntakeId(context.getIntakeId());
        entity.setSessionId(context.getSessionId());
        entity.setApplicationSource(context.getApplicationSource().name());
        entity.setInvitationId(context.getInvitationId());
        entity.setOfferId(context.getOfferId());
        entity.setCustomerReferenceId(context.getCustomerReferenceId());
        entity.setPrefillStatus(context.getPrefillStatus().name());
        entity.setCreatedTimestamp(context.getCreatedTimestamp());
        return entity;
    }

    private ApplicationIntakeContext toDomain(ApplicationIntakeContextJpaEntity entity) {
        return ApplicationIntakeContext.reconstitute(
                entity.getIntakeId(),
                entity.getSessionId(),
                ApplicationSource.valueOf(entity.getApplicationSource()),
                entity.getInvitationId(),
                entity.getOfferId(),
                entity.getCustomerReferenceId(),
                PrefillStatus.valueOf(entity.getPrefillStatus()),
                entity.getCreatedTimestamp()
        );
    }
}
