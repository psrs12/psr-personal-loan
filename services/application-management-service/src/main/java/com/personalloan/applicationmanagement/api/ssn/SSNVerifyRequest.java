package com.personalloan.applicationmanagement.api.ssn;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SSNVerifyRequest(
        @NotBlank
        @Pattern(regexp = "\\d{9}", message = "SSN must be 9 digits")
        String ssn
) {}
