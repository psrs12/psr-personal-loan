package com.personalloan.documentservice.api.document;

import com.personalloan.documentservice.application.document.ProcessVirusScanResultUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal/virus-scan")
public class VirusScanCallbackController {

    private final ProcessVirusScanResultUseCase useCase;

    public VirusScanCallbackController(ProcessVirusScanResultUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/result")
    public ResponseEntity<Void> callback(@RequestBody @Valid VirusScanResultRequest request) {
        useCase.execute(request.documentId(), request.clean(), request.failureReason());
        return ResponseEntity.ok().build();
    }

    public record VirusScanResultRequest(
            @NotNull UUID documentId,
            boolean clean,
            String failureReason) {}
}
