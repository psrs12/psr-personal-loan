package com.personalloan.pricingorchestration.domain.pricing;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationExpiryInfoTest {

    @Test
    void isExpired_trueWhenDateInPast() {
        ApplicationExpiryInfo info = new ApplicationExpiryInfo(LocalDateTime.now().minusDays(1), "PRICING_PENDING");

        assertThat(info.isExpired()).isTrue();
    }

    @Test
    void isExpired_falseWhenDateInFuture() {
        ApplicationExpiryInfo info = new ApplicationExpiryInfo(LocalDateTime.now().plusDays(1), "PRICING_PENDING");

        assertThat(info.isExpired()).isFalse();
    }

    @Test
    void isExpired_falseWhenDateIsNull() {
        ApplicationExpiryInfo info = new ApplicationExpiryInfo(null, "PRICING_PENDING");

        assertThat(info.isExpired()).isFalse();
    }
}
