package com.personalloan.applicationmanagement.api.invitation;

import jakarta.validation.constraints.NotBlank;

public record ValidateInvitationRequest(@NotBlank String token) {}
