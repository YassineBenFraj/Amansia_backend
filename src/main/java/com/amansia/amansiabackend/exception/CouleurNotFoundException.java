package com.amansia.amansiabackend.exception;

public class CouleurNotFoundException extends RuntimeException {

    public CouleurNotFoundException(Integer id) {
        super("La couleur " + id + " est introuvable");
    }
}
