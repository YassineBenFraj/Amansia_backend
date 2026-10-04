package com.amansia.amansiabackend.exception;

public class MateriauInactiveException extends RuntimeException {

    public MateriauInactiveException(Integer id) {
        super("Le materiau " + id + " est inactif");
    }
}
