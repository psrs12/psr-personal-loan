package com.personalloan.documentservice.api.document;

import com.personalloan.documentservice.application.document.UploadDocumentUseCase;
import com.personalloan.documentservice.domain.document.DocumentRequirement;
import com.personalloan.documentservice.domain.document.DocumentType;
import com.personalloan.documentservice.domain.document.port.DocumentRequirementRepository;
import com.personalloan.documentservice.domain.exception.DocumentCountSatisfiedException;
import com.personalloan.documentservice.domain.exception.DocumentTypeNotRequiredException;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/applications/{applicationId}/documents")
public class DocumentController {

    private final DocumentRequirementRepository requirementRepository;
    private final UploadDocumentUseCase uploadDocumentUseCase;

    public DocumentController(DocumentRequirementRepository requirementRepository,
                               UploadDocumentUseCase uploadDocumentUseCase) {
        this.requirementRepository = requirementRepository;
        this.uploadDocumentUseCase = uploadDocumentUseCase;
    }

    @GetMapping("/requirements")
    public List<DocumentRequirementResponse> getRequirements(@PathVariable UUID applicationId) {
        return requirementRepository.findByApplicationId(applicationId).stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentUploadResponse> upload(
            @PathVariable UUID applicationId,
            @RequestParam @NotBlank String documentType,
            @RequestParam("file") MultipartFile file) {

        DocumentType type = DocumentType.valueOf(documentType.toUpperCase());
        UUID documentId = uploadDocumentUseCase.execute(applicationId, type, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new DocumentUploadResponse(documentId, "UPLOADED"));
    }

    private DocumentRequirementResponse toResponse(DocumentRequirement req) {
        return new DocumentRequirementResponse(
                req.getRequirementId(),
                req.getDocumentType().name(),
                req.getCount(),
                req.getDescription(),
                req.getStatus().name()
        );
    }

    public record DocumentRequirementResponse(
            UUID requirementId, String documentType, int count, String description, String status) {}

    public record DocumentUploadResponse(UUID documentId, String status) {}
}
