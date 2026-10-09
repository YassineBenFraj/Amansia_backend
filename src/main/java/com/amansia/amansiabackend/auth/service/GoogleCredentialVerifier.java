package com.amansia.amansiabackend.auth.service;

public interface GoogleCredentialVerifier {
    GoogleIdentity verify(String credential);

    record GoogleIdentity(String subject, String email, boolean emailVerified,
                          String givenName, String familyName, String hostedDomain) {}
}
