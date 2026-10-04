package com.amansia.amansiabackend.exception;

public class AvisAlreadyExistsException extends RuntimeException {

    public AvisAlreadyExistsException(Integer produitId, Integer commandeId) {
        super("Un avis existe déjà pour le produit " + produitId + " dans la commande " + commandeId);
    }
}
