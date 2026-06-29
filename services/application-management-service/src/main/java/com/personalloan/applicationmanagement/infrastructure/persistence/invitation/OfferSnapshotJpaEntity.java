package com.personalloan.applicationmanagement.infrastructure.persistence.invitation;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "offer_snapshot")
public class OfferSnapshotJpaEntity {

    @Id
    @Column(name = "snapshot_id")
    private UUID snapshotId;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "offer_id")
    private String offerId;

    @Column(name = "customer_reference_id")
    private String customerReferenceId;

    @Column(name = "loan_amount")
    private BigDecimal loanAmount;

    @Column(name = "apr")
    private BigDecimal apr;

    @Column(name = "term_months")
    private Integer termMonths;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "retrieved_timestamp", nullable = false)
    private LocalDateTime retrievedTimestamp;

    public UUID getSnapshotId() { return snapshotId; }
    public void setSnapshotId(UUID snapshotId) { this.snapshotId = snapshotId; }
    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }
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
    public LocalDateTime getRetrievedTimestamp() { return retrievedTimestamp; }
    public void setRetrievedTimestamp(LocalDateTime retrievedTimestamp) { this.retrievedTimestamp = retrievedTimestamp; }
}
