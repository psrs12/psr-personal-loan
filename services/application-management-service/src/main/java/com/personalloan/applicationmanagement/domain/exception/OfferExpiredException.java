package com.personalloan.applicationmanagement.domain.exception;

public class OfferExpiredException extends RuntimeException {
    public OfferExpiredException(String offerId) {
        super("Offer has expired: " + offerId);
    }
}
