package com.personalloan.applicationmanagement.api.invitation;

import java.util.UUID;

public record InvitationPrefillResponse(
        UUID intakeId,
        String prefillStatus,
        OfferDetailsResponse offer,
        CustomerPrefillResponse prefill
) {}
