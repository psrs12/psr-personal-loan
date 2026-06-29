package com.personalloan.applicationmanagement.domain.application.port;

import com.personalloan.applicationmanagement.domain.application.Application;

import java.util.Optional;
import java.util.UUID;

public interface ApplicationRepository {
    Application save(Application application);
    Optional<Application> findByApplicationId(UUID applicationId);
    boolean existsActiveApplicationForInvitation(String invitationId);
}
