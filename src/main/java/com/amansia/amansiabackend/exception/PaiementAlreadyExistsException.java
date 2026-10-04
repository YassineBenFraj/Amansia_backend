package com.amansia.amansiabackend.exception;

public class PaiementAlreadyExistsException extends RuntimeException {

    public PaiementAlreadyExistsException(Integer commandeId) {
        super("Un paiement en attente, payé ou remboursé existe pour la commande " + commandeId);
    }
}
