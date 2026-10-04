package com.amansia.amansiabackend.exception;

public class StripeWebhookInvalidException extends RuntimeException {

    public StripeWebhookInvalidException(String message) {
        super(message);
    }

    public StripeWebhookInvalidException(String message, Throwable cause) {
        super(message, cause);
    }
}
