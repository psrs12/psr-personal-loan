package com.personalloan.applicationmanagement.domain.pricing.port;

import com.personalloan.applicationmanagement.domain.pricing.ConsentCapturedEvent;

public interface PricingEventPublisher {
    void publishConsentCaptured(ConsentCapturedEvent event);
}
