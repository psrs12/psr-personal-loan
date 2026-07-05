package com.personalloan.applicationmanagement.infrastructure.messaging;

import com.personalloan.applicationmanagement.domain.application.ApplicationStatus;
import com.personalloan.applicationmanagement.domain.application.event.ESignCompletedEvent;
import com.personalloan.applicationmanagement.domain.application.port.ApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ESignCompletedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ESignCompletedEventConsumer.class);

    private final ApplicationRepository applicationRepository;

    public ESignCompletedEventConsumer(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @KafkaListener(topics = "${kafka.topics.offer-acceptance-events}", groupId = "${spring.kafka.consumer.group-id}",
                   containerFactory = "esignCompletedListenerContainerFactory")
    @Transactional
    public void onESignCompleted(ESignCompletedEvent event) {
        log.info("Received ESignCompleted for application {}", event.applicationId());
        applicationRepository.findByApplicationId(event.applicationId()).ifPresent(application -> {
            application.transitionTo(ApplicationStatus.OFFER_ACCEPTED);
            applicationRepository.save(application);
        });
    }
}
