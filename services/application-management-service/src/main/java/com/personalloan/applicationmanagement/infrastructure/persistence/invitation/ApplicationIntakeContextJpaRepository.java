package com.personalloan.applicationmanagement.infrastructure.persistence.invitation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ApplicationIntakeContextJpaRepository extends JpaRepository<ApplicationIntakeContextJpaEntity, UUID> {
    Optional<ApplicationIntakeContextJpaEntity> findByInvitationId(String invitationId);
}
