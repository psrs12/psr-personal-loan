package com.personalloan.documentservice.api;

import com.personalloan.documentservice.domain.exception.DocumentCountSatisfiedException;
import com.personalloan.documentservice.domain.exception.DocumentTypeNotRequiredException;
import com.personalloan.documentservice.domain.exception.UnknownDocumentTypeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DocumentTypeNotRequiredException.class)
    public ProblemDetail handleDocumentTypeNotRequired(DocumentTypeNotRequiredException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(DocumentCountSatisfiedException.class)
    public ProblemDetail handleDocumentCountSatisfied(DocumentCountSatisfiedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(UnknownDocumentTypeException.class)
    public ProblemDetail handleUnknownDocumentType(UnknownDocumentTypeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}
