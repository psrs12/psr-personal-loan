package com.personalloan.documentservice.domain;

import com.personalloan.documentservice.domain.document.DocumentType;
import com.personalloan.documentservice.domain.exception.UnknownDocumentTypeException;
import com.personalloan.documentservice.infrastructure.external.applicationmanagement.DecisionEngineDocumentCodeMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DecisionEngineDocumentCodeMapperTest {

    private final DecisionEngineDocumentCodeMapper mapper = new DecisionEngineDocumentCodeMapper();

    @ParameterizedTest
    @CsvSource({
            "BANK_STMT_3M, BANK_STATEMENT, 3",
            "BANK_STMT_6M, BANK_STATEMENT, 6",
            "PAYSLIP_2,    PAY_SLIP,       2",
            "PAYSLIP_3,    PAY_SLIP,       3",
            "TAX_RETURN_1Y,TAX_RETURN,     1",
            "TAX_RETURN_2Y,TAX_RETURN,     2",
            "GOV_ID,       GOVERNMENT_ID,  1",
            "PROOF_OF_ADDRESS, PROOF_OF_ADDRESS, 1",
            "EMPLOYMENT_LETTER, EMPLOYMENT_LETTER, 1"
    })
    void mapsKnownCodes(String code, String expectedType, int expectedCount) {
        DecisionEngineDocumentCodeMapper.MappedDocument result = mapper.map(code);
        assertThat(result.documentType()).isEqualTo(DocumentType.valueOf(expectedType));
        assertThat(result.count()).isEqualTo(expectedCount);
    }

    @Test
    void throwsForUnknownCode() {
        assertThatThrownBy(() -> mapper.map("UNKNOWN_CODE"))
                .isInstanceOf(UnknownDocumentTypeException.class);
    }
}
