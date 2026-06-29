package com.personalloan.applicationmanagement.api.invitation;

import jakarta.validation.constraints.NotBlank;

public record InitializeInvitationRequest(
        @NotBlank String invitationId
) {}
