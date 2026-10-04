package com.amansia.amansiabackend.exception;

public class PanierAccessDeniedException extends RuntimeException {

    public PanierAccessDeniedException() {
        super("Vous ne pouvez pas modifier le panier d'un autre client");
    }
}
