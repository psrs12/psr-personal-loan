package com.personalloan.applicationmanagement.infrastructure.external.customer.dto;

public record CustomerProfileResponse(
        String customerReferenceId,
        String firstName,
        String lastName,
        String street,
        String city,
        String state,
        String zip
) {}
