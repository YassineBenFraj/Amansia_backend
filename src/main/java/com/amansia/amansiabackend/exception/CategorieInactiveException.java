package com.amansia.amansiabackend.exception;

public class CategorieInactiveException extends RuntimeException {

    public CategorieInactiveException(Integer id) {
        super("La categorie " + id + " est inactive");
    }
}
