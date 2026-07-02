package com.personalloan.applicationmanagement.infrastructure.persistence.application;

import com.personalloan.applicationmanagement.domain.application.port.ApplicationExpiryConfigRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ApplicationExpiryConfigJpaAdapter implements ApplicationExpiryConfigRepository {

    private final ApplicationExpiryConfigJpaRepository repo;

    public ApplicationExpiryConfigJpaAdapter(ApplicationExpiryConfigJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<Integer> findExpiryThresholdDays(String productType, String channel) {
        return repo.findByProductTypeAndChannel(productType, channel)
                .map(ApplicationExpiryConfigJpaEntity::getExpiryThresholdDays);
    }
}
