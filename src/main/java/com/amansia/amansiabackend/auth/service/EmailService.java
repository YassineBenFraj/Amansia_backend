package com.amansia.amansiabackend.auth.service;

public interface EmailService {
    void verifierConfiguration();
    void envoyerReinitialisation(String destinataire, String token, long dureeMinutes);
    void envoyerTransactionnel(String destinataire, String sujet, String texte, String html);
}
