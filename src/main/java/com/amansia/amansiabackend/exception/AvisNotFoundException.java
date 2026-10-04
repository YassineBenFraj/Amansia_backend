package com.amansia.amansiabackend.exception;

public class AvisNotFoundException extends RuntimeException {

    public AvisNotFoundException(Integer avisId) {
        super("Aucun avis trouvé avec l'identifiant " + avisId);
    }
}
