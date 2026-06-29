package com.personalloan.applicationmanagement.domain.invitation.port;

import com.personalloan.applicationmanagement.domain.invitation.OfferDetails;

public interface OfferManagementPort {
    void validateInvitation(String invitationId);
    OfferDetails retrieveOffer(String invitationId);
}
