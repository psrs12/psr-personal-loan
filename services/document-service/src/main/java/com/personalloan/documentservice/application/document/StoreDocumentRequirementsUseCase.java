package com.personalloan.documentservice.application.document;

import com.personalloan.documentservice.domain.document.DocumentRequirement;
import com.personalloan.documentservice.domain.document.port.ApplicationManagementPort;
import com.personalloan.documentservice.domain.document.port.DocumentRequirementRepository;
import com.personalloan.documentservice.infrastructure.external.applicationmanagement.DecisionEngineDocumentCodeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class StoreDocumentRequirementsUseCase {

    private final DocumentRequirementRepository requirementRepository;
    private final DecisionEngineDocumentCodeMapper mapper;
    private final ApplicationManagementPort applicationManagementPort;

    public StoreDocumentRequirementsUseCase(DocumentRequirementRepository requirementRepository,
                                             DecisionEngineDocumentCodeMapper mapper,
                                             ApplicationManagementPort applicationManagementPort) {
        this.requirementRepository = requirementRepository;
        this.mapper = mapper;
        this.applicationManagementPort = applicationManagementPort;
    }

    @Transactional
    public void execute(UUID applicationId, List<DocumentCodeEntry> documentCodes) {
        for (DocumentCodeEntry entry : documentCodes) {
            DecisionEngineDocumentCodeMapper.MappedDocument mapped = mapper.map(entry.decisionEngineCode());
            DocumentRequirement requirement = DocumentRequirement.create(
                    applicationId, mapped.documentType(), mapped.count(), mapped.description());
            requirementRepository.save(requirement);
        }
        applicationManagementPort.updateApplicationStatus(applicationId, "DOCUMENTS_REQUIRED");
    }

    public record DocumentCodeEntry(String decisionEngineCode, int count) {}
}
