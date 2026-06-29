package com.personalloan.documentservice.application;

import com.personalloan.documentservice.application.document.UploadDocumentUseCase;
import com.personalloan.documentservice.domain.document.*;
import com.personalloan.documentservice.domain.document.port.DocumentEventPublisher;
import com.personalloan.documentservice.domain.document.port.DocumentRecordRepository;
import com.personalloan.documentservice.domain.document.port.DocumentRequirementRepository;
import com.personalloan.documentservice.domain.document.port.StoragePort;
import com.personalloan.documentservice.domain.exception.DocumentCountSatisfiedException;
import com.personalloan.documentservice.domain.exception.DocumentTypeNotRequiredException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadDocumentUseCaseTest {

    @Mock DocumentRequirementRepository requirementRepository;
    @Mock DocumentRecordRepository recordRepository;
    @Mock StoragePort storagePort;
    @Mock DocumentEventPublisher eventPublisher;

    @InjectMocks UploadDocumentUseCase useCase;

    private final UUID applicationId = UUID.randomUUID();

    @Test
    void throwsWhenDocumentTypeNotRequired() {
        when(requirementRepository.findByApplicationIdAndDocumentType(applicationId, DocumentType.GOVERNMENT_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(applicationId, DocumentType.GOVERNMENT_ID,
                new MockMultipartFile("f", "f.pdf", "application/pdf", new byte[]{1})))
                .isInstanceOf(DocumentTypeNotRequiredException.class);
    }

    @Test
    void throwsWhenCountAlreadySatisfied() {
        DocumentRequirement req = DocumentRequirement.reconstitute(
                UUID.randomUUID(), applicationId, DocumentType.PAY_SLIP, 1, "Pay slips",
                DocumentRequirementStatus.UPLOADED, LocalDateTime.now());

        when(requirementRepository.findByApplicationIdAndDocumentType(applicationId, DocumentType.PAY_SLIP))
                .thenReturn(Optional.of(req));

        DocumentRecord existing = DocumentRecord.reconstitute(
                UUID.randomUUID(), applicationId, req.getRequirementId(), DocumentType.PAY_SLIP,
                "s3://ref", DocumentRecordStatus.UPLOADED, LocalDateTime.now());

        when(recordRepository.findByApplicationIdAndDocumentType(applicationId, DocumentType.PAY_SLIP))
                .thenReturn(List.of(existing));

        assertThatThrownBy(() -> useCase.execute(applicationId, DocumentType.PAY_SLIP,
                new MockMultipartFile("f", "f.pdf", "application/pdf", new byte[]{1})))
                .isInstanceOf(DocumentCountSatisfiedException.class);
    }

    @Test
    void storesDocumentAndPublishesEvent() {
        DocumentRequirement req = DocumentRequirement.reconstitute(
                UUID.randomUUID(), applicationId, DocumentType.GOVERNMENT_ID, 1, "Gov ID",
                DocumentRequirementStatus.PENDING, LocalDateTime.now());

        when(requirementRepository.findByApplicationIdAndDocumentType(applicationId, DocumentType.GOVERNMENT_ID))
                .thenReturn(Optional.of(req));
        when(recordRepository.findByApplicationIdAndDocumentType(applicationId, DocumentType.GOVERNMENT_ID))
                .thenReturn(List.of());
        when(storagePort.store(any(), any(), any())).thenReturn("s3://bucket/path");

        useCase.execute(applicationId, DocumentType.GOVERNMENT_ID,
                new MockMultipartFile("f", "f.pdf", "application/pdf", new byte[]{1}));

        verify(recordRepository).save(any());
        verify(requirementRepository, times(2)).save(any());
        verify(eventPublisher).publishDocumentUploaded(eq(applicationId), any(), eq("GOVERNMENT_ID"));
    }
}
