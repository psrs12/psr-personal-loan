package com.personalloan.offeracceptance.api.offer;

import com.personalloan.offeracceptance.application.offer.ESignUseCase;
import com.personalloan.offeracceptance.domain.exception.SessionNotFoundException;
import com.personalloan.offeracceptance.domain.offer.Declaration;
import com.personalloan.offeracceptance.domain.offer.ESignRecord;
import com.personalloan.offeracceptance.domain.port.OfferAcceptanceSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/applications/{applicationId}")
public class OfferAcceptanceController {

    private final OfferAcceptanceSessionRepository sessionRepository;
    private final ESignUseCase eSignUseCase;

    public OfferAcceptanceController(OfferAcceptanceSessionRepository sessionRepository,
                                      ESignUseCase eSignUseCase) {
        this.sessionRepository = sessionRepository;
        this.eSignUseCase = eSignUseCase;
    }

    @GetMapping("/declarations")
    public List<DeclarationResponse> getDeclarations(@PathVariable UUID applicationId) {
        return sessionRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new SessionNotFoundException(applicationId))
                .getDeclarations().stream()
                .map(d -> new DeclarationResponse(d.declarationId(), d.declarationType(),
                        d.title(), d.content(), d.mandatory()))
                .toList();
    }

    @PostMapping("/esign")
    public ResponseEntity<ESignResponse> eSign(@PathVariable UUID applicationId,
                                                @RequestBody @Valid ESignRequest request,
                                                HttpServletRequest httpRequest) {
        String ipAddress = httpRequest.getRemoteAddr();
        ESignRecord record = eSignUseCase.execute(applicationId, request.acceptedDeclarationIds(), ipAddress);
        return ResponseEntity.ok(new ESignResponse(record.getESignId(), record.getSignedAt().toString()));
    }

    public record DeclarationResponse(UUID declarationId, String declarationType,
                                       String title, String content, boolean mandatory) {}

    public record ESignRequest(@NotEmpty Set<UUID> acceptedDeclarationIds) {}

    public record ESignResponse(UUID eSignId, String signedAt) {}
}
