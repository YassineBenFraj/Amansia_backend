package com.amansia.amansiabackend.exception;

public class VarianteProduitNotFoundException extends RuntimeException {

    public VarianteProduitNotFoundException(Integer id) {
        super("La variante produit " + id + " est introuvable");
    }
}
