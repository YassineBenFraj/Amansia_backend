package com.amansia.amansiabackend.exception;

public class CategorieNotFoundException extends RuntimeException {

    public CategorieNotFoundException(Integer id) {
        super("Categorie introuvable avec l'id " + id);
    }
}
