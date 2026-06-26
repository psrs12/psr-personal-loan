package com.personalloan.applicationmanagement.api.invitation;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OfferDetailsResponse(
        String offerId,
        BigDecimal loanAmount,
        BigDecimal apr,
        int termMonths,
        LocalDate expirationDate
) {}
