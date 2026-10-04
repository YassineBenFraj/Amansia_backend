package com.amansia.amansiabackend.exception;

public class PaiementNotFoundException extends RuntimeException {

    public PaiementNotFoundException(Integer paiementId) {
        super("Aucun paiement trouvé avec l'identifiant " + paiementId);
    }
}
