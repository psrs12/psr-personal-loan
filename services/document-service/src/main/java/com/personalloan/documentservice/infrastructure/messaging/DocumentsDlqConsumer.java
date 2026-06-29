package com.personalloan.documentservice.infrastructure.messaging;

import com.personalloan.documentservice.domain.exception.UnknownDocumentTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DocumentsDlqConsumer {

    private static final Logger log = LoggerFactory.getLogger(DocumentsDlqConsumer.class);

    @KafkaListener(topics = "${kafka.topics.pricing-events-dlq:pricing-events.DLT}",
                   groupId = "${spring.kafka.consumer.group-id}-dlq")
    public void onDeadLetter(Object payload) {
        log.error("ALERT: Message sent to DLQ — likely UnknownDocumentTypeException. Payload type: {}. " +
                "Manual intervention required to handle unrecognised Decision Engine document type code.",
                payload != null ? payload.getClass().getSimpleName() : "null");
    }
}
