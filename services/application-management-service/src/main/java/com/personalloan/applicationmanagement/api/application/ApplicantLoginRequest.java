package com.personalloan.applicationmanagement.api.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.UUID;

public record ApplicantLoginRequest(
        @NotNull UUID applicationId,
        @NotBlank @Pattern(regexp = "\\d{4}") String last4SSN,
        @NotNull LocalDate dateOfBirth
) {}
