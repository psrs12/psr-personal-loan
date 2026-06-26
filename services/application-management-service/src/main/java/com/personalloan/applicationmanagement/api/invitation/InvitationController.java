package com.personalloan.applicationmanagement.api.invitation;

import com.personalloan.applicationmanagement.application.invitation.ProcessInvitationResult;
import com.personalloan.applicationmanagement.application.invitation.ProcessInvitationUseCase;
import com.personalloan.applicationmanagement.domain.invitation.CustomerPrefill;
import com.personalloan.applicationmanagement.domain.invitation.OfferDetails;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invitations")
public class InvitationController {

    private final ProcessInvitationUseCase processInvitationUseCase;

    public InvitationController(ProcessInvitationUseCase processInvitationUseCase) {
        this.processInvitationUseCase = processInvitationUseCase;
    }

    @PostMapping("/initialize")
    public ResponseEntity<InvitationPrefillResponse> initialize(
            @RequestHeader("X-Channel-ID") String channelId,
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody InitializeInvitationRequest request) {

        ProcessInvitationResult result = processInvitationUseCase.execute(request.invitationId());

        return ResponseEntity.ok(toResponse(result));
    }

    private InvitationPrefillResponse toResponse(ProcessInvitationResult result) {
        OfferDetails offer = result.offerDetails();
        OfferDetailsResponse offerResponse = offer != null ? new OfferDetailsResponse(
                offer.offerId(),
                offer.loanAmount(),
                offer.apr(),
                offer.termMonths(),
                offer.expirationDate()
        ) : null;

        CustomerPrefill prefill = result.customerPrefill();
        CustomerPrefillResponse prefillResponse = prefill != null ? new CustomerPrefillResponse(
                prefill.firstName(),
                prefill.lastName(),
                prefill.street(),
                prefill.city(),
                prefill.state(),
                prefill.zip()
        ) : null;

        return new InvitationPrefillResponse(
                result.intakeId(),
                result.prefillStatus().name(),
                offerResponse,
                prefillResponse
        );
    }
}
