package com.amansia.amansiabackend.auth.service;

public class EmailIndisponibleException extends RuntimeException {
    public EmailIndisponibleException() {
        super("L'envoi d'email est temporairement indisponible. Veuillez réessayer plus tard.");
    }
}
