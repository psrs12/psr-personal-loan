package com.personalloan.pricingorchestration.infrastructure.persistence.pricing;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pricing_offer")
public class PricingOfferJpaEntity {

    @Id
    @Column(name = "pricing_offer_id")
    private UUID pricingOfferId;

    @Column(name = "application_id", nullable = false)
    private UUID applicationId;

    @Column(name = "approved_amount", nullable = false)
    private BigDecimal approvedAmount;

    @Column(name = "interest_rate", nullable = false)
    private BigDecimal interestRate;

    @Column(name = "apr", nullable = false)
    private BigDecimal apr;

    @Column(name = "term_months", nullable = false)
    private int termMonths;

    @Column(name = "monthly_repayment", nullable = false)
    private BigDecimal monthlyRepayment;

    @Column(name = "total_repayable", nullable = false)
    private BigDecimal totalRepayable;

    @Column(name = "offer_expiry_date", nullable = false)
    private LocalDateTime offerExpiryDate;

    @Column(name = "pricing_model_ref")
    private String pricingModelRef;

    @Column(name = "bureau_snapshot_ref")
    private String bureauSnapshotRef;

    @Column(name = "offer_status", nullable = false)
    private String offerStatus;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public UUID getPricingOfferId() { return pricingOfferId; }
    public void setPricingOfferId(UUID pricingOfferId) { this.pricingOfferId = pricingOfferId; }
    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }
    public BigDecimal getApprovedAmount() { return approvedAmount; }
    public void setApprovedAmount(BigDecimal approvedAmount) { this.approvedAmount = approvedAmount; }
    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }
    public BigDecimal getApr() { return apr; }
    public void setApr(BigDecimal apr) { this.apr = apr; }
    public int getTermMonths() { return termMonths; }
    public void setTermMonths(int termMonths) { this.termMonths = termMonths; }
    public BigDecimal getMonthlyRepayment() { return monthlyRepayment; }
    public void setMonthlyRepayment(BigDecimal monthlyRepayment) { this.monthlyRepayment = monthlyRepayment; }
    public BigDecimal getTotalRepayable() { return totalRepayable; }
    public void setTotalRepayable(BigDecimal totalRepayable) { this.totalRepayable = totalRepayable; }
    public LocalDateTime getOfferExpiryDate() { return offerExpiryDate; }
    public void setOfferExpiryDate(LocalDateTime offerExpiryDate) { this.offerExpiryDate = offerExpiryDate; }
    public String getPricingModelRef() { return pricingModelRef; }
    public void setPricingModelRef(String pricingModelRef) { this.pricingModelRef = pricingModelRef; }
    public String getBureauSnapshotRef() { return bureauSnapshotRef; }
    public void setBureauSnapshotRef(String bureauSnapshotRef) { this.bureauSnapshotRef = bureauSnapshotRef; }
    public String getOfferStatus() { return offerStatus; }
    public void setOfferStatus(String offerStatus) { this.offerStatus = offerStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
