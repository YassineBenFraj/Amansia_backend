package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.PasswordChangeRequest;
import com.amansia.amansiabackend.dto.ProfileUpdateRequest;
import com.amansia.amansiabackend.dto.UtilisateurDTO;
import com.amansia.amansiabackend.entity.Role;
import com.amansia.amansiabackend.entity.Utilisateur;
import com.amansia.amansiabackend.exception.FichierImageInvalideException;
import com.amansia.amansiabackend.repository.UtilisateurRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfileServiceTest {
    @TempDir Path directory;

    @Test
    void updateUsesAuthenticatedEmailAndDoesNotChangeAdministrativeFields() {
        var users = mock(UtilisateurRepository.class);
        var usersService = mock(UtilisateurService.class);
        var user = user();
        when(users.findByEmail("client@example.com")).thenReturn(Optional.of(user));
        when(users.save(user)).thenReturn(user);
        when(usersService.recupererUtilisateurParId(7)).thenReturn(new UtilisateurDTO());
        var profile = new ProfileService(users, usersService, new BCryptPasswordEncoder(), directory.toString());

        profile.update("client@example.com", new ProfileUpdateRequest("Nouveau", "Prénom", "22222222"));

        assertEquals("Nouveau", user.getNom());
        assertEquals("client@example.com", user.getEmail());
        assertEquals(Role.CLIENT, user.getRole());
        assertTrue(user.getActif());
        verify(users).findByEmail("client@example.com");
        verify(users, never()).findById(anyInt());
    }

    @Test
    void passwordChangeRequiresCurrentPasswordAndStoresOnlyBcryptHash() {
        var users = mock(UtilisateurRepository.class);
        var encoder = new BCryptPasswordEncoder();
        var user = user();
        user.setMotDePasse(encoder.encode("ancien-secret"));
        when(users.findByEmail("client@example.com")).thenReturn(Optional.of(user));
        var profile = new ProfileService(users, mock(UtilisateurService.class), encoder, directory.toString());

        assertThrows(IllegalArgumentException.class, () -> profile.changePassword("client@example.com", new PasswordChangeRequest("faux", "nouveau-secret", "nouveau-secret")));
        verify(users, never()).save(any());
        profile.changePassword("client@example.com", new PasswordChangeRequest("ancien-secret", "nouveau-secret", "nouveau-secret"));
        assertTrue(encoder.matches("nouveau-secret", user.getMotDePasse()));
        assertNotEquals("nouveau-secret", user.getMotDePasse());
        verify(users).save(user);
    }

    @Test
    void invalidPhotoIsRejectedBeforeAnyAccountOrFileChange() throws Exception {
        var users = mock(UtilisateurRepository.class);
        var profile = new ProfileService(users, mock(UtilisateurService.class), new BCryptPasswordEncoder(), directory.toString());
        var file = new MockMultipartFile("file", "photo.png", "image/png", "not an image".getBytes());

        assertThrows(FichierImageInvalideException.class, () -> profile.upload("client@example.com", file));
        verifyNoInteractions(users);
        try (var files = Files.list(directory)) { assertEquals(0, files.count()); }
    }

    private Utilisateur user() {
        var user = new Utilisateur();
        user.setId(7);
        user.setEmail("client@example.com");
        user.setRole(Role.CLIENT);
        user.setActif(true);
        return user;
    }
}
