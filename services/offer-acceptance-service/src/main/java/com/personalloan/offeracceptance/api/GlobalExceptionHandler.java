package com.personalloan.offeracceptance.api;

import com.personalloan.offeracceptance.domain.exception.AlreadySignedException;
import com.personalloan.offeracceptance.domain.exception.MandatoryDeclarationMissingException;
import com.personalloan.offeracceptance.domain.exception.SessionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SessionNotFoundException.class)
    public ProblemDetail handleSessionNotFound(SessionNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(AlreadySignedException.class)
    public ProblemDetail handleAlreadySigned(AlreadySignedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(MandatoryDeclarationMissingException.class)
    public ProblemDetail handleMandatoryMissing(MandatoryDeclarationMissingException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }
}
