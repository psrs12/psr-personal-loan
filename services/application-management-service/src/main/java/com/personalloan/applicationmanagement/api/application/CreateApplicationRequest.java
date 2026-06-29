package com.personalloan.applicationmanagement.api.application;

import com.personalloan.applicationmanagement.domain.application.Citizenship;
import com.personalloan.applicationmanagement.domain.application.EmploymentStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateApplicationRequest(
        UUID intakeId,

        @NotBlank String ssnVerificationToken,
        @NotBlank @Pattern(regexp = "\\d{9}", message = "SSN must be 9 digits") String ssn,

        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull LocalDate dateOfBirth,
        @NotNull Citizenship citizenship,

        @NotBlank String email,
        @NotBlank String phone,

        @NotBlank String street,
        @NotBlank String city,
        @NotBlank @Size(min = 2, max = 2) String state,
        @NotBlank String zip,

        String employerName,
        EmploymentStatus employmentStatus,
        @NotNull @DecimalMin("0.00") BigDecimal annualIncome,

        @NotNull @DecimalMin("1000.00") BigDecimal requestedAmount,
        @NotNull @Min(6) @Max(84) Integer termMonths,
        String loanPurpose
) {}
