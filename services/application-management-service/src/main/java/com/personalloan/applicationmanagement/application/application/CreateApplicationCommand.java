package com.personalloan.applicationmanagement.application.application;

import com.personalloan.applicationmanagement.domain.application.Citizenship;
import com.personalloan.applicationmanagement.domain.application.EmploymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateApplicationCommand(
        UUID intakeId,
        String ssnVerificationToken,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        Citizenship citizenship,
        String ssn,
        String email,
        String phone,
        String street,
        String city,
        String state,
        String zip,
        String employerName,
        EmploymentStatus employmentStatus,
        BigDecimal annualIncome,
        BigDecimal requestedAmount,
        int termMonths,
        String loanPurpose
) {}
