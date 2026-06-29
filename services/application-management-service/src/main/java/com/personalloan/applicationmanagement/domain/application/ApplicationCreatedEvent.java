package com.personalloan.applicationmanagement.domain.application;

import com.personalloan.applicationmanagement.domain.invitation.ApplicationSource;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApplicationCreatedEvent(
        UUID applicationId,
        ApplicationSource applicationSource,
        LocalDateTime createdTimestamp
) {}
