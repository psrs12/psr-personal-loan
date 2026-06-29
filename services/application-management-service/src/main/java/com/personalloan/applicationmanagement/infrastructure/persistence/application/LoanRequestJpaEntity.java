package com.personalloan.applicationmanagement.infrastructure.persistence.application;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "loan_request")
public class LoanRequestJpaEntity {

    @Id
    @Column(name = "loan_request_id")
    private UUID loanRequestId;

    @Column(name = "application_id", nullable = false)
    private UUID applicationId;

    @Column(name = "requested_amount", nullable = false)
    private BigDecimal requestedAmount;

    @Column(name = "term_months", nullable = false)
    private Integer termMonths;

    @Column(name = "loan_purpose")
    private String loanPurpose;

    @Column(name = "created_timestamp", nullable = false)
    private LocalDateTime createdTimestamp;

    @Column(name = "updated_timestamp")
    private LocalDateTime updatedTimestamp;

    public UUID getLoanRequestId() { return loanRequestId; }
    public void setLoanRequestId(UUID loanRequestId) { this.loanRequestId = loanRequestId; }
    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }
    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public void setRequestedAmount(BigDecimal requestedAmount) { this.requestedAmount = requestedAmount; }
    public Integer getTermMonths() { return termMonths; }
    public void setTermMonths(Integer termMonths) { this.termMonths = termMonths; }
    public String getLoanPurpose() { return loanPurpose; }
    public void setLoanPurpose(String loanPurpose) { this.loanPurpose = loanPurpose; }
    public LocalDateTime getCreatedTimestamp() { return createdTimestamp; }
    public void setCreatedTimestamp(LocalDateTime createdTimestamp) { this.createdTimestamp = createdTimestamp; }
    public LocalDateTime getUpdatedTimestamp() { return updatedTimestamp; }
    public void setUpdatedTimestamp(LocalDateTime updatedTimestamp) { this.updatedTimestamp = updatedTimestamp; }
}
