package com.personalloan.applicationmanagement.api.common;

import com.personalloan.applicationmanagement.application.pricing.*;
import com.personalloan.applicationmanagement.domain.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleApplicationNotFound(ApplicationNotFoundException ex) {
        return response(HttpStatus.NOT_FOUND, "APPLICATION_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(ApplicationExpiredException.class)
    public ResponseEntity<ErrorResponse> handleApplicationExpired(ApplicationExpiredException ex) {
        return response(HttpStatus.UNPROCESSABLE_ENTITY, "APPLICATION_EXPIRED", ex.getMessage());
    }

    @ExceptionHandler(PricingOfferNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePricingOfferNotFound(PricingOfferNotFoundException ex) {
        return response(HttpStatus.NOT_FOUND, "PRICING_OFFER_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(PricingOfferExpiredException.class)
    public ResponseEntity<ErrorResponse> handlePricingOfferExpired(PricingOfferExpiredException ex) {
        return response(HttpStatus.UNPROCESSABLE_ENTITY, "PRICING_OFFER_EXPIRED", ex.getMessage());
    }

    @ExceptionHandler(InvitationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleInvitationNotFound(InvitationNotFoundException ex) {
        return response(HttpStatus.NOT_FOUND, "INVITATION_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(InvitationExpiredException.class)
    public ResponseEntity<ErrorResponse> handleInvitationExpired(InvitationExpiredException ex) {
        return response(HttpStatus.UNPROCESSABLE_ENTITY, "INVITATION_EXPIRED", ex.getMessage());
    }

    @ExceptionHandler(OfferUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleOfferUnavailable(OfferUnavailableException ex) {
        return response(HttpStatus.SERVICE_UNAVAILABLE, "OFFER_UNAVAILABLE", ex.getMessage());
    }

    @ExceptionHandler(OfferExpiredException.class)
    public ResponseEntity<ErrorResponse> handleOfferExpired(OfferExpiredException ex) {
        return response(HttpStatus.UNPROCESSABLE_ENTITY, "OFFER_EXPIRED", ex.getMessage());
    }

    @ExceptionHandler(DuplicateApplicationException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateApplication(DuplicateApplicationException ex) {
        return response(HttpStatus.UNPROCESSABLE_ENTITY, "DUPLICATE_APPLICATION", ex.getMessage());
    }

    @ExceptionHandler(IntakeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleIntakeNotFound(IntakeNotFoundException ex) {
        return response(HttpStatus.NOT_FOUND, "INTAKE_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(IntakeExpiredException.class)
    public ResponseEntity<ErrorResponse> handleIntakeExpired(IntakeExpiredException ex) {
        return response(HttpStatus.UNPROCESSABLE_ENTITY, "INTAKE_EXPIRED", ex.getMessage());
    }

    @ExceptionHandler(SSNVerificationFailedException.class)
    public ResponseEntity<ErrorResponse> handleSSNVerificationFailed(SSNVerificationFailedException ex) {
        return response(HttpStatus.UNPROCESSABLE_ENTITY, "SSN_VERIFICATION_FAILED", ex.getMessage());
    }

    @ExceptionHandler(SSNVerificationTokenInvalidException.class)
    public ResponseEntity<ErrorResponse> handleSSNVerificationTokenInvalid(SSNVerificationTokenInvalidException ex) {
        return response(HttpStatus.BAD_REQUEST, "SSN_VERIFICATION_TOKEN_INVALID", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message);
    }

    @ExceptionHandler(ApplicantVerificationFailedException.class)
    public ResponseEntity<ErrorResponse> handleApplicantVerificationFailed(ApplicantVerificationFailedException ex) {
        return response(HttpStatus.UNAUTHORIZED, "APPLICANT_VERIFICATION_FAILED", ex.getMessage());
    }

    @ExceptionHandler(ApplicationNotAccessibleException.class)
    public ResponseEntity<ErrorResponse> handleApplicationNotAccessible(ApplicationNotAccessibleException ex) {
        return response(HttpStatus.UNPROCESSABLE_ENTITY, "APPLICATION_NOT_ACCESSIBLE", ex.getMessage());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException ex) {
        return response(HttpStatus.BAD_REQUEST, "MISSING_HEADER", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred");
    }

    private ResponseEntity<ErrorResponse> response(HttpStatus status, String errorCode, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(
                errorCode,
                message,
                UUID.randomUUID().toString(),
                LocalDateTime.now()
        ));
    }
}
