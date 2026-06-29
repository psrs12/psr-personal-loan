package com.personalloan.applicationmanagement.domain.application.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record ESignCompletedEvent(UUID applicationId, LocalDateTime signedAt, UUID correlationId) {}
