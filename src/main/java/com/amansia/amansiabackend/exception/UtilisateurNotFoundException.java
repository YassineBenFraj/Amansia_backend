package com.amansia.amansiabackend.exception;

public class UtilisateurNotFoundException extends RuntimeException {

    public UtilisateurNotFoundException(Integer id) {
        super("Utilisateur introuvable avec l'id " + id);
    }

    public UtilisateurNotFoundException(String field, String value) {
        super("Utilisateur introuvable pour " + field + " " + value);
    }
}
