package com.personalloan.applicationmanagement.infrastructure.logging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SensitiveDataMaskingConverterTest {

    private final SensitiveDataMaskingConverter converter = new SensitiveDataMaskingConverter();

    @Test
    void masks_nineDigitSsn() {
        String masked = converter.maskForTest("Processing applicant with ssn=123456789 completed");
        assertThat(masked).doesNotContain("123456789");
        assertThat(masked).contains("*********");
    }

    @Test
    void masks_dashedSsn() {
        String masked = converter.maskForTest("SSN value: 123-45-6789 received");
        assertThat(masked).doesNotContain("123-45-6789");
        assertThat(masked).contains("***-**-****");
    }

    @Test
    void doesNotMask_shortNumbers() {
        String input = "Application 12345 processed";
        String masked = converter.maskForTest(input);
        assertThat(masked).contains("12345");
    }

    @Test
    void doesNotMask_normalText() {
        String input = "Application created successfully";
        String masked = converter.maskForTest(input);
        assertThat(masked).isEqualTo(input);
    }
}
