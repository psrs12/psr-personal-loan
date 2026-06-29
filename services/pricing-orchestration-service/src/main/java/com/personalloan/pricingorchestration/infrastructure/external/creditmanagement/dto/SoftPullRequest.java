package com.personalloan.pricingorchestration.infrastructure.external.creditmanagement.dto;

import java.util.UUID;

public record SoftPullRequest(UUID applicationId, String applicantReference) {}
