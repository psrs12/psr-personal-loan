package com.personalloan.applicationmanagement.infrastructure.messaging;

import com.personalloan.applicationmanagement.domain.pricing.ConsentCapturedEvent;
import com.personalloan.applicationmanagement.domain.pricing.port.PricingEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaPricingEventPublisher implements PricingEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String consentTopic;

    public KafkaPricingEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                       @Value("${kafka.topics.consent-events}") String consentTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.consentTopic = consentTopic;
    }

    @Override
    public void publishConsentCaptured(ConsentCapturedEvent event) {
        kafkaTemplate.send(consentTopic, event.applicationId().toString(), event);
    }
}
