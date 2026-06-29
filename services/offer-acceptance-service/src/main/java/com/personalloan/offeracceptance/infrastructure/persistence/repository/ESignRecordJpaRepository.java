package com.personalloan.offeracceptance.infrastructure.persistence.repository;

import com.personalloan.offeracceptance.infrastructure.persistence.entity.ESignRecordJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ESignRecordJpaRepository extends JpaRepository<ESignRecordJpaEntity, UUID> {
}
