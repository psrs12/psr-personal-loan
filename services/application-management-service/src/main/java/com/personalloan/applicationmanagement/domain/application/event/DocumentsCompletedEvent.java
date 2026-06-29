package com.personalloan.applicationmanagement.domain.application.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentsCompletedEvent(UUID applicationId, LocalDateTime completedAt, UUID correlationId) {}
