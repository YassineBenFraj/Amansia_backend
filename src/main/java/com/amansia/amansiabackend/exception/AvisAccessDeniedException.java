package com.amansia.amansiabackend.exception;

public class AvisAccessDeniedException extends RuntimeException {

    public AvisAccessDeniedException() {
        super("Vous n'êtes pas autorisé à gérer cet avis");
    }
}
