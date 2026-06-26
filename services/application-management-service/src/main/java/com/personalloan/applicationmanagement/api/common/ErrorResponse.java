package com.personalloan.applicationmanagement.api.common;

import java.time.LocalDateTime;

public record ErrorResponse(
        String errorCode,
        String message,
        String correlationId,
        LocalDateTime timestamp
) {}
