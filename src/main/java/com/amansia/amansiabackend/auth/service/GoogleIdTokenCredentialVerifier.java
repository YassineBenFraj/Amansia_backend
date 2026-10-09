package com.amansia.amansiabackend.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GoogleIdTokenCredentialVerifier implements GoogleCredentialVerifier {
    private static final Logger log = LoggerFactory.getLogger(GoogleIdTokenCredentialVerifier.class);
    private final String clientId;
    private final GoogleIdTokenVerifier verifier;

    public GoogleIdTokenCredentialVerifier(@Value("${app.google.client-id:}") String clientId) {
        this.clientId = clientId.trim();
        this.verifier = this.clientId.isBlank() ? null : new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(List.of(this.clientId)).build();
    }

    @Override
    public GoogleIdentity verify(String credential) {
        if (verifier == null) throw new IllegalStateException("Google Login n'est pas configuré.");
        if (credential == null || credential.isBlank() || credential.length() > 8192)
            throw new IllegalArgumentException("Connexion Google invalide.");
        try {
            GoogleIdToken token = verifier.verify(credential);
            if (token == null) {
                throw new IllegalArgumentException("Connexion Google invalide.");
            }
            var payload = token.getPayload();
            String subject = payload.getSubject();
            String email = payload.getEmail();
            if (!audienceMatches(payload.getAudience()) || subject == null || subject.isBlank() || email == null || email.isBlank()
                    || !Boolean.TRUE.equals(payload.getEmailVerified())) {
                log.warn("Google credential rejected after signature verification: audienceMatches={}, subjectPresent={}, emailPresent={}, emailVerified={}",
                        audienceMatches(payload.getAudience()), subject != null && !subject.isBlank(), email != null && !email.isBlank(), payload.getEmailVerified());
                throw new IllegalArgumentException("Connexion Google invalide.");
            }
            return new GoogleIdentity(subject, email, true,
                    claim(payload, "given_name"), claim(payload, "family_name"), payload.getHostedDomain());
        } catch (IOException | GeneralSecurityException e) {
            log.warn("Google credential verification failed: exceptionType={}, causeType={}",
                    e.getClass().getSimpleName(), e.getCause() == null ? "none" : e.getCause().getClass().getSimpleName());
            throw new IllegalArgumentException("Connexion Google invalide.");
        }
    }

    private boolean audienceMatches(Object audience) {
        if (audience instanceof String value) return clientId.equals(value);
        if (audience instanceof Collection<?> values) return values.contains(clientId);
        return false;
    }

    private static String claim(GoogleIdToken.Payload payload, String key) {
        Object value = payload.get(key);
        return value instanceof String text ? text : "";
    }
}
