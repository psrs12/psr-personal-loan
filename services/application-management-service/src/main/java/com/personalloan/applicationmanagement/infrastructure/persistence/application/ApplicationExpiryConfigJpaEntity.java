package com.personalloan.applicationmanagement.infrastructure.persistence.application;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "application_expiry_config")
public class ApplicationExpiryConfigJpaEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "product_type", nullable = false)
    private String productType;

    @Column(name = "channel", nullable = false)
    private String channel;

    @Column(name = "expiry_threshold_days", nullable = false)
    private Integer expiryThresholdDays;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public Integer getExpiryThresholdDays() { return expiryThresholdDays; }
    public void setExpiryThresholdDays(Integer expiryThresholdDays) { this.expiryThresholdDays = expiryThresholdDays; }
}
