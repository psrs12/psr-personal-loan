package com.personalloan.applicationmanagement.infrastructure.persistence.application;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ApplicantJpaRepository extends JpaRepository<ApplicantJpaEntity, UUID> {}
