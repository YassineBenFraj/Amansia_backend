package com.amansia.amansiabackend.exception;

public class CategorieNameAlreadyUsedException extends RuntimeException {

    public CategorieNameAlreadyUsedException() {
        super("Le nom de categorie est deja utilise");
    }
}
