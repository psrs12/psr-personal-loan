package com.personalloan.applicationmanagement.domain.invitation;

public record CustomerPrefill(
        String firstName,
        String lastName,
        String street,
        String city,
        String state,
        String zip
) {}
