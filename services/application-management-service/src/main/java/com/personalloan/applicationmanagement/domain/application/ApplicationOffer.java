package com.personalloan.applicationmanagement.domain.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class ApplicationOffer {

    private final UUID applicationOfferId;
    private final UUID applicationId;
    private final String offerId;
    private final String customerReferenceId;
    private final BigDecimal loanAmount;
    private final BigDecimal apr;
    private final int termMonths;
    private final LocalDate expirationDate;
    private final LocalDateTime capturedTimestamp;

    private ApplicationOffer(UUID applicationOfferId, UUID applicationId, String offerId,
                              String customerReferenceId, BigDecimal loanAmount, BigDecimal apr,
                              int termMonths, LocalDate expirationDate, LocalDateTime capturedTimestamp) {
        this.applicationOfferId = applicationOfferId;
        this.applicationId = applicationId;
        this.offerId = offerId;
        this.customerReferenceId = customerReferenceId;
        this.loanAmount = loanAmount;
        this.apr = apr;
        this.termMonths = termMonths;
        this.expirationDate = expirationDate;
        this.capturedTimestamp = capturedTimestamp;
    }

    public static ApplicationOffer capture(UUID applicationId, String offerId, String customerReferenceId,
                                            BigDecimal loanAmount, BigDecimal apr, int termMonths, LocalDate expirationDate) {
        return new ApplicationOffer(UUID.randomUUID(), applicationId, offerId, customerReferenceId,
                loanAmount, apr, termMonths, expirationDate, LocalDateTime.now());
    }

    public static ApplicationOffer reconstitute(UUID applicationOfferId, UUID applicationId, String offerId,
                                                 String customerReferenceId, BigDecimal loanAmount, BigDecimal apr,
                                                 int termMonths, LocalDate expirationDate, LocalDateTime capturedTimestamp) {
        return new ApplicationOffer(applicationOfferId, applicationId, offerId, customerReferenceId,
                loanAmount, apr, termMonths, expirationDate, capturedTimestamp);
    }

    public UUID getApplicationOfferId() { return applicationOfferId; }
    public UUID getApplicationId() { return applicationId; }
    public String getOfferId() { return offerId; }
    public String getCustomerReferenceId() { return customerReferenceId; }
    public BigDecimal getLoanAmount() { return loanAmount; }
    public BigDecimal getApr() { return apr; }
    public int getTermMonths() { return termMonths; }
    public LocalDate getExpirationDate() { return expirationDate; }
    public LocalDateTime getCapturedTimestamp() { return capturedTimestamp; }
}
