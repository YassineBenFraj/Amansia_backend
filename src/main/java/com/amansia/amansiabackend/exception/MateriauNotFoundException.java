package com.amansia.amansiabackend.exception;

public class MateriauNotFoundException extends RuntimeException {

    public MateriauNotFoundException(Integer id) {
        super("Le materiau " + id + " est introuvable");
    }
}
