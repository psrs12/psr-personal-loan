package com.personalloan.pricingorchestration.api.pricing;

import com.personalloan.pricingorchestration.application.pricing.CaptureConsentCommand;
import com.personalloan.pricingorchestration.application.pricing.CaptureConsentUseCase;
import com.personalloan.pricingorchestration.application.pricing.GetPricingOffersUseCase;
import com.personalloan.pricingorchestration.application.pricing.GetSelectedOfferUseCase;
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
    private final GetSelectedOfferUseCase getSelectedOfferUseCase;
    private final SelectOfferUseCase selectOfferUseCase;
    private final CaptureConsentUseCase captureConsentUseCase;

    public PricingController(GetPricingOffersUseCase getPricingOffersUseCase,
                              GetSelectedOfferUseCase getSelectedOfferUseCase,
                              SelectOfferUseCase selectOfferUseCase,
                              CaptureConsentUseCase captureConsentUseCase) {
        this.getPricingOffersUseCase = getPricingOffersUseCase;
        this.getSelectedOfferUseCase = getSelectedOfferUseCase;
        this.selectOfferUseCase = selectOfferUseCase;
        this.captureConsentUseCase = captureConsentUseCase;
    }

    @GetMapping("/pricing-offers")
    public ResponseEntity<List<PricingOfferResponse>> getPricingOffers(@PathVariable UUID applicationId) {
        List<PricingOffer> offers = getPricingOffersUseCase.execute(applicationId);
        List<PricingOfferResponse> response = offers.stream().map(PricingOfferResponse::from).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/selected-offer")
    public ResponseEntity<PricingOfferResponse> getSelectedOffer(@PathVariable UUID applicationId) {
        PricingOffer offer = getSelectedOfferUseCase.execute(applicationId);
        return ResponseEntity.ok(PricingOfferResponse.from(offer));
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
