package com.personalloan.documentservice.domain.document.port;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface StoragePort {
    String store(UUID applicationId, String documentType, MultipartFile file);
}
