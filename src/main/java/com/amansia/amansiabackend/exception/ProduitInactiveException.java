package com.amansia.amansiabackend.exception;

public class ProduitInactiveException extends RuntimeException {

    public ProduitInactiveException(Integer id) {
        super("Le produit " + id + " est inactif");
    }
}
