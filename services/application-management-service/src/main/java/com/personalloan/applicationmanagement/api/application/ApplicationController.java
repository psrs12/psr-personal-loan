package com.personalloan.applicationmanagement.api.application;

import com.personalloan.applicationmanagement.application.application.ApplicantLoginResult;
import com.personalloan.applicationmanagement.application.application.ApplicantLoginUseCase;
import com.personalloan.applicationmanagement.application.application.CreateApplicationCommand;
import com.personalloan.applicationmanagement.application.application.CreateApplicationUseCase;
import com.personalloan.applicationmanagement.application.application.GetApplicationTimelineUseCase;
import com.personalloan.applicationmanagement.domain.application.Application;
import com.personalloan.applicationmanagement.domain.application.ApplicationAuditRecord;
import com.personalloan.applicationmanagement.domain.application.port.ApplicationRepository;
import com.personalloan.applicationmanagement.domain.exception.ApplicationNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

    private final CreateApplicationUseCase createApplicationUseCase;
    private final GetApplicationTimelineUseCase getApplicationTimelineUseCase;
    private final ApplicantLoginUseCase applicantLoginUseCase;
    private final ApplicationRepository applicationRepository;

    public ApplicationController(CreateApplicationUseCase createApplicationUseCase,
                                  GetApplicationTimelineUseCase getApplicationTimelineUseCase,
                                  ApplicantLoginUseCase applicantLoginUseCase,
                                  ApplicationRepository applicationRepository) {
        this.createApplicationUseCase = createApplicationUseCase;
        this.getApplicationTimelineUseCase = getApplicationTimelineUseCase;
        this.applicantLoginUseCase = applicantLoginUseCase;
        this.applicationRepository = applicationRepository;
    }

    @PostMapping
    public ResponseEntity<CreateApplicationResponse> create(
            @RequestHeader("X-Channel-ID") String channelId,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody CreateApplicationRequest request) {

        CreateApplicationCommand command = new CreateApplicationCommand(
                request.intakeId(),
                request.ssnVerificationToken(),
                request.firstName(),
                request.lastName(),
                request.dateOfBirth(),
                request.citizenship(),
                request.ssn(),
                request.email(),
                request.phone(),
                request.street(),
                request.city(),
                request.state(),
                request.zip(),
                request.employerName(),
                request.employmentStatus(),
                request.annualIncome(),
                request.requestedAmount(),
                request.termMonths(),
                request.loanPurpose()
        );

        UUID applicationId = createApplicationUseCase.execute(command);

        Application application = applicationRepository.findByApplicationId(applicationId)
                .orElseThrow();

        return ResponseEntity.ok(new CreateApplicationResponse(
                application.getApplicationId(),
                application.getApplicationStatus().name(),
                application.getApplicationSource().name(),
                application.getCreatedTimestamp()
        ));
    }

    @GetMapping("/{applicationId}/status")
    public ResponseEntity<ApplicationStatusResponse> getStatus(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID applicationId) {

        Application application = applicationRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException(applicationId));

        return ResponseEntity.ok(new ApplicationStatusResponse(
                application.getApplicationId(),
                application.getApplicationStatus().name()
        ));
    }

    @GetMapping("/{applicationId}/timeline")
    public ResponseEntity<ApplicationTimelineResponse> getTimeline(
            @RequestHeader("X-Channel-ID") String channelId,
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID applicationId) {

        List<ApplicationAuditRecord> records = getApplicationTimelineUseCase.execute(applicationId);

        List<ApplicationTimelineResponse.TimelineEvent> events = records.stream()
                .map(r -> new ApplicationTimelineResponse.TimelineEvent(
                        r.auditId(), r.eventType(), r.eventTimestamp(), r.intakeId(), r.payload()))
                .toList();

        return ResponseEntity.ok(new ApplicationTimelineResponse(applicationId, events));
    }

    @PostMapping("/login")
    public ResponseEntity<ApplicantLoginResponse> login(@Valid @RequestBody ApplicantLoginRequest request) {
        ApplicantLoginResult result = applicantLoginUseCase.execute(
                request.applicationId(), request.last4SSN(), request.dateOfBirth());
        return ResponseEntity.ok(new ApplicantLoginResponse(
                result.sessionToken(), result.expiresAt(), result.applicationId(), result.applicationStatus()));
    }
}
