package com.amansia.amansiabackend.exception;

public class CouleurInactiveException extends RuntimeException {

    public CouleurInactiveException(Integer id) {
        super("La couleur " + id + " est inactive");
    }
}
