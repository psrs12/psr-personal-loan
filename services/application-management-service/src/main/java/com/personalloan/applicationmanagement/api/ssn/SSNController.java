package com.personalloan.applicationmanagement.api.ssn;

import com.personalloan.applicationmanagement.application.application.VerifySSNUseCase;
import com.personalloan.applicationmanagement.domain.application.SSNVerificationToken;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ssn")
public class SSNController {

    private final VerifySSNUseCase verifySSNUseCase;

    public SSNController(VerifySSNUseCase verifySSNUseCase) {
        this.verifySSNUseCase = verifySSNUseCase;
    }

    @PostMapping("/verify")
    public ResponseEntity<SSNVerifyResponse> verify(
            @RequestHeader("X-Channel-ID") String channelId,
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody SSNVerifyRequest request) {

        SSNVerificationToken token = verifySSNUseCase.execute(request.ssn());

        return ResponseEntity.ok(new SSNVerifyResponse(token.token(), token.expiresAt()));
    }
}
