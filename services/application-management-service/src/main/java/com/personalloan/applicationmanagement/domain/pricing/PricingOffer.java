package com.personalloan.applicationmanagement.domain.pricing;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PricingOffer {

    private final UUID pricingOfferId;
    private final UUID applicationId;
    private final BigDecimal approvedAmount;
    private final BigDecimal interestRate;
    private final BigDecimal apr;
    private final int termMonths;
    private final BigDecimal monthlyRepayment;
    private final BigDecimal totalRepayable;
    private final LocalDateTime offerExpiryDate;
    private final String pricingModelRef;
    private final String bureauSnapshotRef;
    private PricingOfferStatus offerStatus;
    private final LocalDateTime createdAt;

    private PricingOffer(UUID pricingOfferId, UUID applicationId, BigDecimal approvedAmount,
                         BigDecimal interestRate, BigDecimal apr, int termMonths,
                         BigDecimal monthlyRepayment, BigDecimal totalRepayable,
                         LocalDateTime offerExpiryDate, String pricingModelRef,
                         String bureauSnapshotRef, PricingOfferStatus offerStatus,
                         LocalDateTime createdAt) {
        this.pricingOfferId = pricingOfferId;
        this.applicationId = applicationId;
        this.approvedAmount = approvedAmount;
        this.interestRate = interestRate;
        this.apr = apr;
        this.termMonths = termMonths;
        this.monthlyRepayment = monthlyRepayment;
        this.totalRepayable = totalRepayable;
        this.offerExpiryDate = offerExpiryDate;
        this.pricingModelRef = pricingModelRef;
        this.bureauSnapshotRef = bureauSnapshotRef;
        this.offerStatus = offerStatus;
        this.createdAt = createdAt;
    }

    public static PricingOffer create(UUID applicationId, BigDecimal approvedAmount,
                                      BigDecimal interestRate, BigDecimal apr, int termMonths,
                                      BigDecimal monthlyRepayment, BigDecimal totalRepayable,
                                      LocalDateTime offerExpiryDate, String pricingModelRef,
                                      String bureauSnapshotRef) {
        return new PricingOffer(UUID.randomUUID(), applicationId, approvedAmount, interestRate, apr,
                termMonths, monthlyRepayment, totalRepayable, offerExpiryDate,
                pricingModelRef, bureauSnapshotRef, PricingOfferStatus.ACTIVE, LocalDateTime.now());
    }

    public static PricingOffer reconstitute(UUID pricingOfferId, UUID applicationId,
                                             BigDecimal approvedAmount, BigDecimal interestRate,
                                             BigDecimal apr, int termMonths, BigDecimal monthlyRepayment,
                                             BigDecimal totalRepayable, LocalDateTime offerExpiryDate,
                                             String pricingModelRef, String bureauSnapshotRef,
                                             PricingOfferStatus offerStatus, LocalDateTime createdAt) {
        return new PricingOffer(pricingOfferId, applicationId, approvedAmount, interestRate, apr,
                termMonths, monthlyRepayment, totalRepayable, offerExpiryDate,
                pricingModelRef, bureauSnapshotRef, offerStatus, createdAt);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(offerExpiryDate);
    }

    public void supersede() {
        this.offerStatus = PricingOfferStatus.SUPERSEDED;
    }

    public UUID getPricingOfferId() { return pricingOfferId; }
    public UUID getApplicationId() { return applicationId; }
    public BigDecimal getApprovedAmount() { return approvedAmount; }
    public BigDecimal getInterestRate() { return interestRate; }
    public BigDecimal getApr() { return apr; }
    public int getTermMonths() { return termMonths; }
    public BigDecimal getMonthlyRepayment() { return monthlyRepayment; }
    public BigDecimal getTotalRepayable() { return totalRepayable; }
    public LocalDateTime getOfferExpiryDate() { return offerExpiryDate; }
    public String getPricingModelRef() { return pricingModelRef; }
    public String getBureauSnapshotRef() { return bureauSnapshotRef; }
    public PricingOfferStatus getOfferStatus() { return offerStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
