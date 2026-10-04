package com.amansia.amansiabackend.exception;

public class LignePanierNotFoundException extends RuntimeException {

    public LignePanierNotFoundException(Integer id) {
        super("La ligne du panier " + id + " est introuvable");
    }
}
