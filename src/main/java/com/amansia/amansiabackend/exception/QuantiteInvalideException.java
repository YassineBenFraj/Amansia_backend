package com.amansia.amansiabackend.exception;

public class QuantiteInvalideException extends RuntimeException {

    public QuantiteInvalideException() {
        super("La quantité doit être strictement supérieure à zéro");
    }
}
