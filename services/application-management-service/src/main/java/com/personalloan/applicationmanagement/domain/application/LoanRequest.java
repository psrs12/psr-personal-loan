package com.personalloan.applicationmanagement.domain.application;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class LoanRequest {

    private final UUID loanRequestId;
    private final UUID applicationId;
    private final BigDecimal requestedAmount;
    private final int termMonths;
    private final String loanPurpose;
    private final LocalDateTime createdTimestamp;

    private LoanRequest(UUID loanRequestId, UUID applicationId, BigDecimal requestedAmount,
                         int termMonths, String loanPurpose, LocalDateTime createdTimestamp) {
        this.loanRequestId = loanRequestId;
        this.applicationId = applicationId;
        this.requestedAmount = requestedAmount;
        this.termMonths = termMonths;
        this.loanPurpose = loanPurpose;
        this.createdTimestamp = createdTimestamp;
    }

    public static LoanRequest create(UUID applicationId, BigDecimal requestedAmount, int termMonths, String loanPurpose) {
        return new LoanRequest(UUID.randomUUID(), applicationId, requestedAmount, termMonths, loanPurpose, LocalDateTime.now());
    }

    public static LoanRequest reconstitute(UUID loanRequestId, UUID applicationId, BigDecimal requestedAmount,
                                            int termMonths, String loanPurpose, LocalDateTime createdTimestamp) {
        return new LoanRequest(loanRequestId, applicationId, requestedAmount, termMonths, loanPurpose, createdTimestamp);
    }

    public UUID getLoanRequestId() { return loanRequestId; }
    public UUID getApplicationId() { return applicationId; }
    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public int getTermMonths() { return termMonths; }
    public String getLoanPurpose() { return loanPurpose; }
    public LocalDateTime getCreatedTimestamp() { return createdTimestamp; }
}
