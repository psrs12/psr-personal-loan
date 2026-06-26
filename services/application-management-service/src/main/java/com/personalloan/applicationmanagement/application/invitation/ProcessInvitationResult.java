package com.personalloan.applicationmanagement.application.invitation;

import com.personalloan.applicationmanagement.domain.invitation.CustomerPrefill;
import com.personalloan.applicationmanagement.domain.invitation.OfferDetails;
import com.personalloan.applicationmanagement.domain.invitation.PrefillStatus;

import java.util.UUID;

public record ProcessInvitationResult(
        UUID intakeId,
        UUID sessionId,
        OfferDetails offerDetails,
        CustomerPrefill customerPrefill,
        PrefillStatus prefillStatus
) {}
