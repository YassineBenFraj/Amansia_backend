package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.auth.service.GoogleIdTokenCredentialVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class GoogleCredentialVerifierTest {
    @Test void signedTokenWithAudienceListIsAccepted() throws Exception {
        GoogleIdTokenCredentialVerifier service = new GoogleIdTokenCredentialVerifier("expected-client-id");
        GoogleIdTokenVerifier google = mock(GoogleIdTokenVerifier.class);
        GoogleIdToken token = mock(GoogleIdToken.class);
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setAudience(java.util.List.of("expected-client-id"));
        payload.setSubject("sub-1");
        payload.setEmail("client@gmail.com");
        payload.setEmailVerified(true);
        when(google.verify("credential")).thenReturn(token);
        when(token.getPayload()).thenReturn(payload);
        ReflectionTestUtils.setField(service, "verifier", google);
        org.junit.jupiter.api.Assertions.assertEquals("sub-1", service.verify("credential").subject());
    }

    @Test void wrongAudienceIsRejectedWithoutCallingGoogleServers() throws Exception {
        GoogleIdTokenCredentialVerifier service = new GoogleIdTokenCredentialVerifier("expected-client-id");
        GoogleIdTokenVerifier google = mock(GoogleIdTokenVerifier.class);
        GoogleIdToken token = mock(GoogleIdToken.class);
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setAudience("another-client-id");
        payload.setSubject("sub-1");
        payload.setEmail("client@gmail.com");
        payload.setEmailVerified(true);
        when(google.verify("credential")).thenReturn(token);
        when(token.getPayload()).thenReturn(payload);
        ReflectionTestUtils.setField(service, "verifier", google);
        assertThrows(IllegalArgumentException.class, () -> service.verify("credential"));
    }

    @Test void invalidGoogleTokenIsRejected() throws Exception {
        GoogleIdTokenCredentialVerifier service = new GoogleIdTokenCredentialVerifier("expected-client-id");
        GoogleIdTokenVerifier google = mock(GoogleIdTokenVerifier.class);
        when(google.verify("credential")).thenReturn(null);
        ReflectionTestUtils.setField(service, "verifier", google);
        assertThrows(IllegalArgumentException.class, () -> service.verify("credential"));
    }
}
