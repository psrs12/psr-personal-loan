package com.personalloan.pricingorchestration.domain.pricing.port;

import com.personalloan.pricingorchestration.domain.pricing.FinalDecisionResponse;
import com.personalloan.pricingorchestration.domain.pricing.PricingEngineResponse;
import com.personalloan.pricingorchestration.domain.pricing.PricingRequest;

import java.util.UUID;

public interface DecisionPlatformPort {
    PricingEngineResponse requestPricing(PricingRequest request);
    FinalDecisionResponse requestFinalDecision(UUID applicationId, UUID selectedPricingOfferId,
                                                String hardPullCreditReportReferenceId);
}
