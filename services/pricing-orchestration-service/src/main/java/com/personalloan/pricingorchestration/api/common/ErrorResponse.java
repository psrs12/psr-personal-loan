package com.personalloan.pricingorchestration.api.common;

import java.time.LocalDateTime;

public record ErrorResponse(
        String errorCode,
        String message,
        String correlationId,
        LocalDateTime timestamp
) {}
