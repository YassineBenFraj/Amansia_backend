package com.amansia.amansiabackend.exception;

public class CouleurNameAlreadyUsedException extends RuntimeException {

    public CouleurNameAlreadyUsedException() {
        super("Le nom de la couleur est deja utilise");
    }
}
