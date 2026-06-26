package com.personalloan.applicationmanagement.domain.invitation.port;

import com.personalloan.applicationmanagement.domain.invitation.InvitationSession;

import java.util.Optional;
import java.util.UUID;

public interface InvitationSessionRepository {
    InvitationSession save(InvitationSession session);
    Optional<InvitationSession> findBySessionId(UUID sessionId);
}
