package com.personalloan.documentservice.application.document;

import com.personalloan.documentservice.domain.document.*;
import com.personalloan.documentservice.domain.document.port.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProcessVirusScanResultUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessVirusScanResultUseCase.class);

    private final DocumentRequirementRepository requirementRepository;
    private final DocumentRecordRepository recordRepository;
    private final DocumentEventPublisher eventPublisher;
    private final ApplicationManagementPort applicationManagementPort;

    public ProcessVirusScanResultUseCase(DocumentRequirementRepository requirementRepository,
                                          DocumentRecordRepository recordRepository,
                                          DocumentEventPublisher eventPublisher,
                                          ApplicationManagementPort applicationManagementPort) {
        this.requirementRepository = requirementRepository;
        this.recordRepository = recordRepository;
        this.eventPublisher = eventPublisher;
        this.applicationManagementPort = applicationManagementPort;
    }

    @Transactional
    public void execute(UUID documentId, boolean clean, String failureReason) {
        DocumentRecord record = recordRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));

        UUID applicationId = record.getApplicationId();

        if (clean) {
            record.markVerified();
            recordRepository.save(record);

            requirementRepository.findByApplicationIdAndDocumentType(applicationId, record.getDocumentType())
                    .ifPresent(req -> {
                        long verified = recordRepository.findByApplicationIdAndDocumentType(applicationId, record.getDocumentType())
                                .stream().filter(r -> r.getStatus() == DocumentRecordStatus.VERIFIED).count();
                        if (verified >= req.getCount()) {
                            req.markCompleted();
                            requirementRepository.save(req);
                        }
                    });

            evaluateAllComplete(applicationId);
        } else {
            record.markRejected();
            recordRepository.save(record);

            requirementRepository.findByApplicationIdAndDocumentType(applicationId, record.getDocumentType())
                    .ifPresent(req -> {
                        req.markRejected();
                        requirementRepository.save(req);
                    });

            eventPublisher.publishDocumentRejected(applicationId, documentId, failureReason);
        }
    }

    private void evaluateAllComplete(UUID applicationId) {
        List<DocumentRequirement> requirements = requirementRepository.findByApplicationId(applicationId);
        boolean allComplete = !requirements.isEmpty() &&
                requirements.stream().allMatch(r -> r.getStatus() == DocumentRequirementStatus.COMPLETED);

        if (allComplete) {
            eventPublisher.publishDocumentsCompleted(applicationId);
            applicationManagementPort.updateApplicationStatus(applicationId, "UNDERWRITING");
            log.info("All documents completed for application {}", applicationId);
        }
    }
}
