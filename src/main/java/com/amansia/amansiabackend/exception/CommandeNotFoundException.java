package com.amansia.amansiabackend.exception;

public class CommandeNotFoundException extends RuntimeException {

    public CommandeNotFoundException(Integer commandeId) {
        super("Aucune commande trouvée avec l'identifiant " + commandeId);
    }
}
