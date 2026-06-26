package com.personalloan.applicationmanagement.domain.invitation;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OfferDetails(
        String offerId,
        String customerReferenceId,
        BigDecimal loanAmount,
        BigDecimal apr,
        int termMonths,
        LocalDate expirationDate
) {}
