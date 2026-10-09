package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.auth.dto.LoginRequest;
import com.amansia.amansiabackend.auth.service.AuthService;
import com.amansia.amansiabackend.auth.service.GoogleCredentialVerifier;
import com.amansia.amansiabackend.entity.Role;
import com.amansia.amansiabackend.entity.Utilisateur;
import com.amansia.amansiabackend.repository.UtilisateurRepository;
import com.amansia.amansiabackend.security.JwtUtil;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleAuthServiceTest {
    @Mock UtilisateurRepository users;
    @Mock PasswordEncoder passwords;
    @Mock AuthenticationManager manager;
    @Mock JwtUtil jwt;
    @Mock GoogleCredentialVerifier verifier;
    AuthService service;

    @BeforeEach void setup() { service = new AuthService(users, passwords, manager, jwt, verifier); }

    private GoogleCredentialVerifier.GoogleIdentity identity(String email, String domain) {
        return new GoogleCredentialVerifier.GoogleIdentity("google-sub-1", email, true, "Alice", "Client", domain);
    }

    private Utilisateur existing(Role role) {
        Utilisateur user = new Utilisateur();
        user.setId(7); user.setEmail("alice@gmail.com"); user.setPrenom("Alice"); user.setNom("Client");
        user.setMotDePasse("$2a$existing-bcrypt"); user.setRole(role); user.setActif(true);
        return user;
    }

    @Test void newGoogleUserIsClientAndReceivesAmansiaJwt() {
        when(verifier.verify("credential")).thenReturn(identity("alice@gmail.com", null));
        when(users.findByGoogleSub("google-sub-1")).thenReturn(Optional.empty());
        when(users.findByEmailForUpdate("alice@gmail.com")).thenReturn(Optional.empty());
        when(passwords.encode(anyString())).thenReturn("$2a$random-hash");
        when(users.saveAndFlush(any())).thenAnswer(inv -> { Utilisateur u = inv.getArgument(0); u.setId(7); return u; });
        when(jwt.generateToken("alice@gmail.com", "CLIENT")).thenReturn("amansia-jwt");
        var result = service.google("credential");
        assertEquals("amansia-jwt", result.token());
        assertEquals(Role.CLIENT, result.utilisateur().getRole());
        verify(users).saveAndFlush(argThat(u -> u.getRole() == Role.CLIENT && u.getGoogleSub().equals("google-sub-1")
                && u.getMotDePasse().equals("$2a$random-hash")));
    }

    @Test void linkedGoogleUserSignsInWithoutDuplicate() {
        Utilisateur user = existing(Role.CLIENT); user.setGoogleSub("google-sub-1");
        when(verifier.verify("credential")).thenReturn(identity("alice@gmail.com", null));
        when(users.findByGoogleSub("google-sub-1")).thenReturn(Optional.of(user));
        service.google("credential");
        verify(users, never()).saveAndFlush(any());
        verify(users, never()).save(any());
    }

    @Test void classicPasswordAndAdminRoleRemainUnchangedWhenLinked() {
        Utilisateur user = existing(Role.ADMINISTRATEUR);
        when(verifier.verify("credential")).thenReturn(identity("alice@gmail.com", null));
        when(users.findByGoogleSub("google-sub-1")).thenReturn(Optional.empty());
        when(users.findByEmailForUpdate("alice@gmail.com")).thenReturn(Optional.of(user));
        service.google("credential");
        assertEquals(7, user.getId());
        assertEquals("google-sub-1", user.getGoogleSub());
        assertEquals("$2a$existing-bcrypt", user.getMotDePasse());
        assertEquals(Role.ADMINISTRATEUR, user.getRole());
        verify(passwords, never()).encode(anyString());
    }

    @Test void thirdPartyEmailCannotAutomaticallyLinkAnExistingAccount() {
        Utilisateur user = existing(Role.CLIENT); user.setEmail("alice@example.com");
        when(verifier.verify("credential")).thenReturn(identity("alice@example.com", null));
        when(users.findByGoogleSub("google-sub-1")).thenReturn(Optional.empty());
        when(users.findByEmailForUpdate("alice@example.com")).thenReturn(Optional.of(user));
        assertThrows(IllegalArgumentException.class, () -> service.google("credential"));
        assertNull(user.getGoogleSub());
    }

    @Test void authenticatedThirdPartyAccountCanLinkWithoutChangingPasswordOrRole() {
        Utilisateur user = existing(Role.CLIENT); user.setEmail("alice@example.com");
        when(verifier.verify("credential")).thenReturn(identity("alice@example.com", null));
        when(users.findByEmailForUpdate("alice@example.com")).thenReturn(Optional.of(user));
        when(users.findByGoogleSub("google-sub-1")).thenReturn(Optional.empty());
        var result = service.associerGoogle("alice@example.com", "credential");
        assertEquals(7, result.getId());
        assertEquals("google-sub-1", user.getGoogleSub());
        assertEquals("$2a$existing-bcrypt", user.getMotDePasse());
        assertEquals(Role.CLIENT, user.getRole());
    }

    @Test void authenticatedLinkRejectsDifferentEmail() {
        when(verifier.verify("credential")).thenReturn(identity("other@gmail.com", null));
        assertThrows(IllegalArgumentException.class, () -> service.associerGoogle("alice@gmail.com", "credential"));
        verify(users, never()).save(any());
    }

    @Test void unverifiedEmailAndInvalidCredentialAreRejected() {
        when(verifier.verify("unverified")).thenReturn(new GoogleCredentialVerifier.GoogleIdentity("sub", "a@gmail.com", false, "A", "B", null));
        when(verifier.verify("invalid")).thenThrow(new IllegalArgumentException("Connexion Google invalide."));
        assertThrows(IllegalArgumentException.class, () -> service.google("unverified"));
        assertThrows(IllegalArgumentException.class, () -> service.google("invalid"));
        verify(users, never()).saveAndFlush(any());
    }

    @Test void classicLoginStillUsesAuthenticationManager() {
        Authentication auth = mock(Authentication.class);
        when(manager.authenticate(any())).thenReturn(auth);
        when(auth.getName()).thenReturn("alice@gmail.com");
        when(users.findByEmail("alice@gmail.com")).thenReturn(Optional.of(existing(Role.CLIENT)));
        service.login(new LoginRequest("alice@gmail.com", "existing-password"));
        verify(manager).authenticate(any());
    }
}
