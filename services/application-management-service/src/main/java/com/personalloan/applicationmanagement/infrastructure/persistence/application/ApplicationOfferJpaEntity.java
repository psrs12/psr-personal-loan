package com.personalloan.applicationmanagement.infrastructure.persistence.application;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "application_offer")
public class ApplicationOfferJpaEntity {

    @Id
    @Column(name = "application_offer_id")
    private UUID applicationOfferId;

    @Column(name = "application_id", nullable = false)
    private UUID applicationId;

    @Column(name = "offer_id", nullable = false)
    private String offerId;

    @Column(name = "customer_reference_id", nullable = false)
    private String customerReferenceId;

    @Column(name = "loan_amount")
    private BigDecimal loanAmount;

    @Column(name = "apr")
    private BigDecimal apr;

    @Column(name = "term_months")
    private Integer termMonths;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "captured_timestamp", nullable = false)
    private LocalDateTime capturedTimestamp;

    public UUID getApplicationOfferId() { return applicationOfferId; }
    public void setApplicationOfferId(UUID applicationOfferId) { this.applicationOfferId = applicationOfferId; }
    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }
    public String getOfferId() { return offerId; }
    public void setOfferId(String offerId) { this.offerId = offerId; }
    public String getCustomerReferenceId() { return customerReferenceId; }
    public void setCustomerReferenceId(String customerReferenceId) { this.customerReferenceId = customerReferenceId; }
    public BigDecimal getLoanAmount() { return loanAmount; }
    public void setLoanAmount(BigDecimal loanAmount) { this.loanAmount = loanAmount; }
    public BigDecimal getApr() { return apr; }
    public void setApr(BigDecimal apr) { this.apr = apr; }
    public Integer getTermMonths() { return termMonths; }
    public void setTermMonths(Integer termMonths) { this.termMonths = termMonths; }
    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }
    public LocalDateTime getCapturedTimestamp() { return capturedTimestamp; }
    public void setCapturedTimestamp(LocalDateTime capturedTimestamp) { this.capturedTimestamp = capturedTimestamp; }
}
