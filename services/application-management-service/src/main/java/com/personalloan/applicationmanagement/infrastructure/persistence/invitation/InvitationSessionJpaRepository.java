package com.personalloan.applicationmanagement.infrastructure.persistence.invitation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InvitationSessionJpaRepository extends JpaRepository<InvitationSessionJpaEntity, UUID> {}
