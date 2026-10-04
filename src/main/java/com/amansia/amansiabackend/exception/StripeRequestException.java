package com.amansia.amansiabackend.exception;

public class StripeRequestException extends RuntimeException {

    public StripeRequestException(String message) {
        super(message);
    }

    public StripeRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
