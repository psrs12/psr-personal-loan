package com.personalloan.pricingorchestration.api.common;

import com.personalloan.pricingorchestration.application.pricing.PricingOfferExpiredException;
import com.personalloan.pricingorchestration.application.pricing.PricingOfferNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PricingOfferNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePricingOfferNotFound(PricingOfferNotFoundException ex) {
        return response(HttpStatus.NOT_FOUND, "PRICING_OFFER_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(PricingOfferExpiredException.class)
    public ResponseEntity<ErrorResponse> handlePricingOfferExpired(PricingOfferExpiredException ex) {
        return response(HttpStatus.UNPROCESSABLE_ENTITY, "PRICING_OFFER_EXPIRED", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message);
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
