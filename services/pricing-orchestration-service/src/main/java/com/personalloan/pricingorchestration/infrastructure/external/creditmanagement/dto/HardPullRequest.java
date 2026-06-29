package com.personalloan.pricingorchestration.infrastructure.external.creditmanagement.dto;

import java.util.UUID;

public record HardPullRequest(UUID applicationId, String applicantReference, String softPullReferenceId) {}
