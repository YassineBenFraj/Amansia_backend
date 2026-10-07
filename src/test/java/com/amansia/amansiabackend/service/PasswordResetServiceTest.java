package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.auth.dto.ResetPasswordRequest;
import com.amansia.amansiabackend.auth.service.EmailService;
import com.amansia.amansiabackend.auth.service.LienReinitialisationException;
import com.amansia.amansiabackend.auth.service.PasswordResetService;
import com.amansia.amansiabackend.entity.JetonReinitialisationMdp;
import com.amansia.amansiabackend.entity.Utilisateur;
import com.amansia.amansiabackend.repository.JetonReinitialisationMdpRepository;
import com.amansia.amansiabackend.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PasswordResetServiceTest {
    private final UtilisateurRepository utilisateurs = mock(UtilisateurRepository.class);
    private final JetonReinitialisationMdpRepository jetons = mock(JetonReinitialisationMdpRepository.class);
    private final PasswordEncoder passwords = mock(PasswordEncoder.class);
    private final EmailService email = mock(EmailService.class);
    private PasswordResetService service;

    @BeforeEach void setup() { service = new PasswordResetService(utilisateurs, jetons, passwords, email, 30, 2); }

    private Utilisateur utilisateur() {
        var user = new Utilisateur(); user.setId(7); user.setEmail("client@example.test"); user.setActif(true); return user;
    }

    @Test void unknownAddressGetsTheSameGenericResponseWithoutToken() {
        assertEquals(PasswordResetService.GENERIC_MESSAGE, service.demander("missing@example.test"));
        verify(email).verifierConfiguration();
        verify(jetons, never()).save(any());
        verify(email, never()).envoyerReinitialisation(anyString(), anyString(), anyLong());
    }

    @Test void storesOnlyHashAndSendsRandomSingleUseLink() throws Exception {
        when(utilisateurs.findByEmailForUpdate("client@example.test")).thenReturn(Optional.of(utilisateur()));
        when(jetons.findTopByUtilisateurIdOrderByDateCreationDesc(7)).thenReturn(Optional.empty());
        assertEquals(PasswordResetService.GENERIC_MESSAGE, service.demander(" CLIENT@example.test "));
        var saved = ArgumentCaptor.forClass(JetonReinitialisationMdp.class);
        verify(jetons).save(saved.capture());
        var sent = ArgumentCaptor.forClass(String.class);
        verify(email).envoyerReinitialisation(eq("client@example.test"), sent.capture(), eq(30L));
        String raw = sent.getValue();
        assertEquals(43, raw.length());
        assertEquals(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(raw.getBytes(StandardCharsets.UTF_8))), saved.getValue().getTokenHash());
        assertNotEquals(raw, saved.getValue().getTokenHash());
        assertTrue(saved.getValue().getDateExpiration().isAfter(LocalDateTime.now().plusMinutes(29)));
        verify(jetons).invaliderJetonsActifs(7);
    }

    @Test void cooldownPreventsRepeatedValidTokens() {
        when(utilisateurs.findByEmailForUpdate("client@example.test")).thenReturn(Optional.of(utilisateur()));
        var previous = new JetonReinitialisationMdp(); previous.setDateCreation(LocalDateTime.now().minusSeconds(20));
        when(jetons.findTopByUtilisateurIdOrderByDateCreationDesc(7)).thenReturn(Optional.of(previous));
        assertEquals(PasswordResetService.GENERIC_MESSAGE, service.demander("client@example.test"));
        verify(jetons, never()).save(any());
        verify(email, never()).envoyerReinitialisation(anyString(), anyString(), anyLong());
    }

    @Test void resetChangesBcryptPasswordOnlyOnce() {
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(new byte[32]);
        var user = utilisateur();
        var saved = new JetonReinitialisationMdp(); saved.setUtilisateur(user);
        saved.setDateExpiration(LocalDateTime.now().plusMinutes(20));
        when(jetons.findUtilisateurIdByTokenHash(anyString())).thenReturn(Optional.of(7));
        when(utilisateurs.findByIdForUpdate(7)).thenReturn(Optional.of(user));
        when(jetons.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(saved));
        when(passwords.encode("nouveau123")).thenReturn("$2a$hash");
        var request = new ResetPasswordRequest(token, "nouveau123", "nouveau123");
        service.reinitialiser(request);
        assertEquals("$2a$hash", user.getMotDePasse());
        assertTrue(saved.isUtilise());
        assertThrows(LienReinitialisationException.class, () -> service.reinitialiser(request));
        verify(passwords, times(1)).encode("nouveau123");
    }

    @Test void expiredOrMismatchedRequestsNeverChangePassword() {
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(new byte[32]);
        var user = utilisateur();
        var saved = new JetonReinitialisationMdp(); saved.setUtilisateur(user);
        saved.setDateExpiration(LocalDateTime.now().minusSeconds(1));
        when(jetons.findUtilisateurIdByTokenHash(anyString())).thenReturn(Optional.of(7));
        when(utilisateurs.findByIdForUpdate(7)).thenReturn(Optional.of(user));
        when(jetons.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(saved));
        assertThrows(LienReinitialisationException.class,
                () -> service.reinitialiser(new ResetPasswordRequest(token, "nouveau123", "nouveau123")));
        assertThrows(IllegalArgumentException.class,
                () -> service.reinitialiser(new ResetPasswordRequest(token, "nouveau123", "different")));
        verifyNoInteractions(passwords);
    }
}
