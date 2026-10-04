package com.amansia.amansiabackend.exception;

public class CommandeAccessDeniedException extends RuntimeException {

    public CommandeAccessDeniedException() {
        super("Vous n'êtes pas autorisé à accéder à cette commande");
    }
}
