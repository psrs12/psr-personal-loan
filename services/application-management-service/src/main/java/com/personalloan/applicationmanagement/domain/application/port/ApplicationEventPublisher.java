package com.personalloan.applicationmanagement.domain.application.port;

import com.personalloan.applicationmanagement.domain.application.ApplicationCreatedEvent;

public interface ApplicationEventPublisher {
    void publishApplicationCreated(ApplicationCreatedEvent event);
}
