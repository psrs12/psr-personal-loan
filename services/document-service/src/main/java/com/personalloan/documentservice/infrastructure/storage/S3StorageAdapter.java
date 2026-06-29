package com.personalloan.documentservice.infrastructure.storage;

import com.personalloan.documentservice.domain.document.port.StoragePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Component
public class S3StorageAdapter implements StoragePort {

    private static final Logger log = LoggerFactory.getLogger(S3StorageAdapter.class);

    @Override
    public String store(UUID applicationId, String documentType, MultipartFile file) {
        String ref = "s3://document-service/" + applicationId + "/" + documentType + "/" + UUID.randomUUID();
        log.info("Stub: stored document at {}", ref);
        return ref;
    }
}
