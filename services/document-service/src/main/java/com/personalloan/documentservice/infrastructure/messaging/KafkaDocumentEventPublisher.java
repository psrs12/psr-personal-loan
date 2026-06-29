package com.personalloan.documentservice.infrastructure.messaging;

import com.personalloan.documentservice.domain.document.port.DocumentEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
public class KafkaDocumentEventPublisher implements DocumentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaDocumentEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public KafkaDocumentEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                        @Value("${kafka.topics.document-events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publishDocumentUploaded(UUID applicationId, UUID documentId, String documentType) {
        Map<String, Object> event = Map.of(
                "eventType", "DocumentUploaded",
                "applicationId", applicationId.toString(),
                "documentId", documentId.toString(),
                "documentType", documentType,
                "occurredAt", LocalDateTime.now().toString()
        );
        kafkaTemplate.send(topic, applicationId.toString(), event);
        log.info("Published DocumentUploaded for application {}", applicationId);
    }

    @Override
    public void publishDocumentsCompleted(UUID applicationId) {
        Map<String, Object> event = Map.of(
                "eventType", "DocumentsCompleted",
                "applicationId", applicationId.toString(),
                "occurredAt", LocalDateTime.now().toString()
        );
        kafkaTemplate.send(topic, applicationId.toString(), event);
        log.info("Published DocumentsCompleted for application {}", applicationId);
    }

    @Override
    public void publishDocumentRejected(UUID applicationId, UUID documentId, String reason) {
        Map<String, Object> event = Map.of(
                "eventType", "DocumentRejected",
                "applicationId", applicationId.toString(),
                "documentId", documentId.toString(),
                "reason", reason,
                "occurredAt", LocalDateTime.now().toString()
        );
        kafkaTemplate.send(topic, applicationId.toString(), event);
        log.info("Published DocumentRejected for application {} document {}", applicationId, documentId);
    }
}
