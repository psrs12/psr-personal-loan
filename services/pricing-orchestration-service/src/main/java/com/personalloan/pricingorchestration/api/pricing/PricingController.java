package com.personalloan.pricingorchestration.api.pricing;

import com.personalloan.pricingorchestration.application.pricing.CaptureConsentCommand;
import com.personalloan.pricingorchestration.application.pricing.CaptureConsentUseCase;
import com.personalloan.pricingorchestration.application.pricing.GetPricingOffersUseCase;
import com.personalloan.pricingorchestration.application.pricing.SelectOfferCommand;
import com.personalloan.pricingorchestration.application.pricing.SelectOfferUseCase;
import com.personalloan.pricingorchestration.domain.pricing.PricingOffer;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/applications/{applicationId}")
public class PricingController {

    private final GetPricingOffersUseCase getPricingOffersUseCase;
    private final SelectOfferUseCase selectOfferUseCase;
    private final CaptureConsentUseCase captureConsentUseCase;

    public PricingController(GetPricingOffersUseCase getPricingOffersUseCase,
                              SelectOfferUseCase selectOfferUseCase,
                              CaptureConsentUseCase captureConsentUseCase) {
        this.getPricingOffersUseCase = getPricingOffersUseCase;
        this.selectOfferUseCase = selectOfferUseCase;
        this.captureConsentUseCase = captureConsentUseCase;
    }

    @GetMapping("/pricing-offers")
    public ResponseEntity<List<PricingOfferResponse>> getPricingOffers(@PathVariable UUID applicationId) {
        List<PricingOffer> offers = getPricingOffersUseCase.execute(applicationId);
        List<PricingOfferResponse> response = offers.stream().map(PricingOfferResponse::from).toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/offer-selection")
    public ResponseEntity<Void> selectOffer(@PathVariable UUID applicationId,
                                            @Valid @RequestBody SelectOfferRequest request) {
        selectOfferUseCase.execute(new SelectOfferCommand(applicationId, request.selectedPricingOfferId()));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/consent")
    public ResponseEntity<Void> captureConsent(@PathVariable UUID applicationId,
                                               @Valid @RequestBody CaptureConsentRequest request) {
        captureConsentUseCase.execute(new CaptureConsentCommand(
                applicationId,
                request.selectedPricingOfferId(),
                request.consentChannel(),
                request.applicantReference()));
        return ResponseEntity.ok().build();
    }
}
