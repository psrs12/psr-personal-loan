package com.personalloan.applicationmanagement.domain.invitation.port;

import com.personalloan.applicationmanagement.domain.invitation.ApplicationIntakeContext;

import java.util.Optional;
import java.util.UUID;

public interface ApplicationIntakeContextRepository {
    ApplicationIntakeContext save(ApplicationIntakeContext context);
    Optional<ApplicationIntakeContext> findByIntakeId(UUID intakeId);
    Optional<ApplicationIntakeContext> findByInvitationId(String invitationId);
}
