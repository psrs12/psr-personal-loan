package com.personalloan.applicationmanagement.infrastructure.external.offer.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RetrieveOfferResponse(
        String offerId,
        String customerReferenceId,
        BigDecimal loanAmount,
        BigDecimal apr,
        int termMonths,
        LocalDate expirationDate
) {}
