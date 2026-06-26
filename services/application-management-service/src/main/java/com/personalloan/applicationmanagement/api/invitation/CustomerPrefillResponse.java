package com.personalloan.applicationmanagement.api.invitation;

public record CustomerPrefillResponse(
        String firstName,
        String lastName,
        String street,
        String city,
        String state,
        String zip
) {}
