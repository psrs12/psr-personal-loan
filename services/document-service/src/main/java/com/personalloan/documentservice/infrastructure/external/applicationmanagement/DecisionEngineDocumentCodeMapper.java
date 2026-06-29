package com.personalloan.documentservice.infrastructure.external.applicationmanagement;

import com.personalloan.documentservice.domain.document.DocumentType;
import com.personalloan.documentservice.domain.exception.UnknownDocumentTypeException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DecisionEngineDocumentCodeMapper {

    public record MappedDocument(DocumentType documentType, int count, String description) {}

    private static final Map<String, MappedDocument> MAPPING = Map.of(
            "BANK_STMT_3M",      new MappedDocument(DocumentType.BANK_STATEMENT,   3, "Last 3 months of bank statements"),
            "BANK_STMT_6M",      new MappedDocument(DocumentType.BANK_STATEMENT,   6, "Last 6 months of bank statements"),
            "PAYSLIP_2",         new MappedDocument(DocumentType.PAY_SLIP,          2, "Last 2 payslips"),
            "PAYSLIP_3",         new MappedDocument(DocumentType.PAY_SLIP,          3, "Last 3 payslips"),
            "TAX_RETURN_1Y",     new MappedDocument(DocumentType.TAX_RETURN,        1, "Most recent tax return"),
            "TAX_RETURN_2Y",     new MappedDocument(DocumentType.TAX_RETURN,        2, "Last 2 years tax returns"),
            "GOV_ID",            new MappedDocument(DocumentType.GOVERNMENT_ID,     1, "Government-issued photo ID"),
            "PROOF_OF_ADDRESS",  new MappedDocument(DocumentType.PROOF_OF_ADDRESS,  1, "Proof of current address"),
            "EMPLOYMENT_LETTER", new MappedDocument(DocumentType.EMPLOYMENT_LETTER, 1, "Employer confirmation letter")
    );

    public MappedDocument map(String decisionEngineCode) {
        MappedDocument mapped = MAPPING.get(decisionEngineCode);
        if (mapped == null) {
            throw new UnknownDocumentTypeException(decisionEngineCode);
        }
        return mapped;
    }
}
