package com.personalloan.applicationmanagement.api.invitation;

import com.personalloan.applicationmanagement.application.invitation.ProcessInvitationResult;
import com.personalloan.applicationmanagement.application.invitation.ProcessInvitationUseCase;
import com.personalloan.applicationmanagement.domain.invitation.CustomerPrefill;
import com.personalloan.applicationmanagement.domain.invitation.OfferDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/invitations")
public class InvitationController {

    private final ProcessInvitationUseCase processInvitationUseCase;

    public InvitationController(ProcessInvitationUseCase processInvitationUseCase) {
        this.processInvitationUseCase = processInvitationUseCase;
    }

    // UI-facing endpoint: POST /invitations/validate  { token }
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate(@Valid @RequestBody ValidateInvitationRequest request) {
        ProcessInvitationResult result = processInvitationUseCase.execute(request.token());
        CustomerPrefill prefill = result.customerPrefill();
        OfferDetails offer = result.offerDetails();
        Map<String, Object> address = prefill != null ? Map.of(
                "line1", prefill.street() != null ? prefill.street() : "",
                "city", prefill.city() != null ? prefill.city() : "",
                "state", prefill.state() != null ? prefill.state() : "",
                "postcode", prefill.zip() != null ? prefill.zip() : ""
        ) : Map.of();
        return ResponseEntity.ok(Map.of(
                "intakeId", result.intakeId().toString(),
                "firstName", prefill != null && prefill.firstName() != null ? prefill.firstName() : "",
                "lastName", prefill != null && prefill.lastName() != null ? prefill.lastName() : "",
                "address", address,
                "offerId", offer != null && offer.offerId() != null ? offer.offerId() : "",
                "requestedAmount", offer != null && offer.loanAmount() != null ? offer.loanAmount() : 0,
                "requestedTermMonths", offer != null && offer.termMonths() != null ? offer.termMonths() : 0
        ));
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
