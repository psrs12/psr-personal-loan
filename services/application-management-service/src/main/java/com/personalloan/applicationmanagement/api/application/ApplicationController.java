package com.personalloan.applicationmanagement.api.application;

import com.personalloan.applicationmanagement.application.application.CreateApplicationCommand;
import com.personalloan.applicationmanagement.application.application.CreateApplicationUseCase;
import com.personalloan.applicationmanagement.domain.application.Application;
import com.personalloan.applicationmanagement.domain.application.port.ApplicationRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

    private final CreateApplicationUseCase createApplicationUseCase;
    private final ApplicationRepository applicationRepository;

    public ApplicationController(CreateApplicationUseCase createApplicationUseCase,
                                  ApplicationRepository applicationRepository) {
        this.createApplicationUseCase = createApplicationUseCase;
        this.applicationRepository = applicationRepository;
    }

    @PostMapping
    public ResponseEntity<CreateApplicationResponse> create(
            @RequestHeader("X-Channel-ID") String channelId,
            @RequestHeader("Authorization") String authorization,
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
}
