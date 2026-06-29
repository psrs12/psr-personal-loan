package com.personalloan.applicationmanagement.infrastructure.messaging;

import com.personalloan.applicationmanagement.domain.application.ApplicationStatus;
import com.personalloan.applicationmanagement.domain.application.event.DocumentsCompletedEvent;
import com.personalloan.applicationmanagement.domain.application.port.ApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DocumentsCompletedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(DocumentsCompletedEventConsumer.class);

    private final ApplicationRepository applicationRepository;

    public DocumentsCompletedEventConsumer(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @KafkaListener(topics = "${kafka.topics.document-events}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void onDocumentsCompleted(DocumentsCompletedEvent event) {
        log.info("Received DocumentsCompleted for application {}", event.applicationId());
        applicationRepository.findByApplicationId(event.applicationId()).ifPresent(application -> {
            application.transitionTo(ApplicationStatus.UNDERWRITING);
            applicationRepository.save(application);
        });
    }
}
