package com.personalloan.pricingorchestration.infrastructure.messaging;

import com.personalloan.pricingorchestration.domain.pricing.FinalDecisionResponse;
import com.personalloan.pricingorchestration.domain.pricing.event.ConsentCapturedEvent;
import com.personalloan.pricingorchestration.domain.pricing.port.PricingEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class KafkaPricingEventPublisher implements PricingEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String pricingEventsTopic;
    private final String consentEventsTopic;

    public KafkaPricingEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                       @Value("${kafka.topics.pricing-events}") String pricingEventsTopic,
                                       @Value("${kafka.topics.consent-events}") String consentEventsTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.pricingEventsTopic = pricingEventsTopic;
        this.consentEventsTopic = consentEventsTopic;
    }

    @Override
    public void publishConsentCaptured(ConsentCapturedEvent event) {
        kafkaTemplate.send(consentEventsTopic, event.applicationId().toString(), event);
    }

    @Override
    public void publishSoftPullInitiated(UUID applicationId) {
        publish(applicationId, "SoftPullInitiated", Map.of("applicationId", applicationId));
    }

    @Override
    public void publishSoftPullCompleted(UUID applicationId, String creditReportReferenceId) {
        publish(applicationId, "SoftPullCompleted",
                Map.of("applicationId", applicationId, "creditReportReferenceId", creditReportReferenceId));
    }

    @Override
    public void publishSoftPullFailed(UUID applicationId, String reason) {
        publish(applicationId, "SoftPullFailed",
                Map.of("applicationId", applicationId, "reason", reason != null ? reason : "unknown"));
    }

    @Override
    public void publishPricingOffersReceived(UUID applicationId) {
        publish(applicationId, "PricingOffersReceived", Map.of("applicationId", applicationId));
    }

    @Override
    public void publishPricingDeclined(UUID applicationId, String reasonCode) {
        publish(applicationId, "PricingDeclined",
                Map.of("applicationId", applicationId, "reasonCode", reasonCode != null ? reasonCode : "unknown"));
    }

    @Override
    public void publishHardPullInitiated(UUID applicationId) {
        publish(applicationId, "HardPullInitiated", Map.of("applicationId", applicationId));
    }

    @Override
    public void publishHardPullCompleted(UUID applicationId, String creditReportReferenceId) {
        publish(applicationId, "HardPullCompleted",
                Map.of("applicationId", applicationId, "creditReportReferenceId", creditReportReferenceId));
    }

    @Override
    public void publishHardPullFailed(UUID applicationId, String reason) {
        publish(applicationId, "HardPullFailed",
                Map.of("applicationId", applicationId, "reason", reason != null ? reason : "unknown"));
    }

    @Override
    public void publishFinalDecisionApproved(UUID applicationId) {
        publish(applicationId, "FinalDecisionApproved", Map.of("applicationId", applicationId));
    }

    @Override
    public void publishFinalDecisionDeclined(UUID applicationId, String reasonCode) {
        publish(applicationId, "FinalDecisionDeclined",
                Map.of("applicationId", applicationId, "reasonCode", reasonCode != null ? reasonCode : "unknown"));
    }

    @Override
    public void publishFinalDecisionReferred(UUID applicationId) {
        publish(applicationId, "FinalDecisionReferred", Map.of("applicationId", applicationId));
    }

    @Override
    public void publishFinalDecisionDocumentsRequired(UUID applicationId, List<FinalDecisionResponse.DocumentCode> documents) {
        publish(applicationId, "FinalDecisionDocumentsRequired",
                Map.of("applicationId", applicationId, "documents", documents));
    }

    private void publish(UUID applicationId, String eventType, Object payload) {
        kafkaTemplate.send(pricingEventsTopic, applicationId.toString(), payload);
    }
}
