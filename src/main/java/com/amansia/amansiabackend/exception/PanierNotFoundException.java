package com.amansia.amansiabackend.exception;

public class PanierNotFoundException extends RuntimeException {

    public PanierNotFoundException(Integer utilisateurId) {
        super("Aucun panier n'a été trouvé pour l'utilisateur " + utilisateurId);
    }
}
