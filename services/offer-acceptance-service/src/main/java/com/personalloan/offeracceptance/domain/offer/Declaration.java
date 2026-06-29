package com.personalloan.offeracceptance.domain.offer;

import java.util.UUID;

public record Declaration(
        UUID declarationId,
        String declarationType,
        String title,
        String content,
        boolean mandatory
) {
    public static Declaration of(String declarationType, String title, String content, boolean mandatory) {
        return new Declaration(UUID.randomUUID(), declarationType, title, content, mandatory);
    }
}
