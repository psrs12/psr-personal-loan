package com.personalloan.offeracceptance.domain.exception;

import java.util.List;
import java.util.UUID;

public class MandatoryDeclarationMissingException extends RuntimeException {
    public MandatoryDeclarationMissingException(List<UUID> missingIds) {
        super("Mandatory declarations not accepted: " + missingIds);
    }
}
