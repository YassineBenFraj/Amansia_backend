package com.amansia.amansiabackend.exception;

public class MateriauNameAlreadyUsedException extends RuntimeException {

    public MateriauNameAlreadyUsedException() {
        super("Le nom du materiau est deja utilise");
    }
}
