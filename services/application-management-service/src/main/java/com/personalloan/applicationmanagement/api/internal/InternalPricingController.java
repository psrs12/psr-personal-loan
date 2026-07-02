package com.personalloan.applicationmanagement.api.internal;

import com.personalloan.applicationmanagement.application.pricing.InternalPricingSyncUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal/applications/{applicationId}")
public class InternalPricingController {

    private final InternalPricingSyncUseCase internalPricingSyncUseCase;

    public InternalPricingController(InternalPricingSyncUseCase internalPricingSyncUseCase) {
        this.internalPricingSyncUseCase = internalPricingSyncUseCase;
    }

    @GetMapping("/pricing-data")
    public ResponseEntity<InternalPricingDataResponse> getPricingData(@PathVariable UUID applicationId) {
        return ResponseEntity.ok(internalPricingSyncUseCase.getPricingData(applicationId));
    }

    @PatchMapping("/status")
    public ResponseEntity<Void> updateStatus(@PathVariable UUID applicationId,
                                              @RequestBody InternalStatusRequest request) {
        internalPricingSyncUseCase.updateStatus(applicationId, request.status());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/soft-pull-reference")
    public ResponseEntity<Void> persistSoftPullReference(@PathVariable UUID applicationId,
                                                           @RequestBody InternalCreditReferenceRequest request) {
        internalPricingSyncUseCase.persistSoftPullReference(applicationId, request.creditReportReferenceId());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/hard-pull-reference")
    public ResponseEntity<Void> persistHardPullReference(@PathVariable UUID applicationId,
                                                           @RequestBody InternalCreditReferenceRequest request) {
        internalPricingSyncUseCase.persistHardPullReference(applicationId, request.creditReportReferenceId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/audit-events")
    public ResponseEntity<Void> recordAuditEvent(@PathVariable UUID applicationId,
                                                  @RequestBody InternalAuditEventRequest request) {
        internalPricingSyncUseCase.recordAuditEvent(applicationId, request.eventType(), request.payload());
        return ResponseEntity.ok().build();
    }
}
