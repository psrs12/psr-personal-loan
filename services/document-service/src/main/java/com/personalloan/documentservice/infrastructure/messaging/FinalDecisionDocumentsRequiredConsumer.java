package com.personalloan.documentservice.infrastructure.messaging;

import com.personalloan.documentservice.application.document.StoreDocumentRequirementsUseCase;
import com.personalloan.documentservice.domain.document.event.FinalDecisionDocumentsRequiredEvent;
import com.personalloan.documentservice.domain.exception.UnknownDocumentTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class FinalDecisionDocumentsRequiredConsumer {

    private static final Logger log = LoggerFactory.getLogger(FinalDecisionDocumentsRequiredConsumer.class);

    private final StoreDocumentRequirementsUseCase useCase;

    public FinalDecisionDocumentsRequiredConsumer(StoreDocumentRequirementsUseCase useCase) {
        this.useCase = useCase;
    }

    @KafkaListener(topics = "${kafka.topics.pricing-events}", groupId = "${spring.kafka.consumer.group-id}",
                   containerFactory = "documentsRequiredListenerContainerFactory")
    public void onDocumentsRequired(FinalDecisionDocumentsRequiredEvent event,
                                     @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("Received FinalDecisionDocumentsRequired for application {}", event.applicationId());
        try {
            useCase.execute(event.applicationId(),
                    event.documents().stream()
                            .map(d -> new StoreDocumentRequirementsUseCase.DocumentCodeEntry(d.decisionEngineCode(), d.count()))
                            .toList());
        } catch (UnknownDocumentTypeException e) {
            log.error("Unknown document type code in event for application {}: {}", event.applicationId(), e.getMessage());
            throw e;
        }
    }
}
