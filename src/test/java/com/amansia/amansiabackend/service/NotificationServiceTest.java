package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.entity.Utilisateur;
import com.amansia.amansiabackend.repository.FavoriRepository;
import com.amansia.amansiabackend.repository.NotificationRepository;
import com.amansia.amansiabackend.repository.UtilisateurRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationServiceTest {
    @Test void foreignNotificationIsNeverReadOrModified() {
        var notifications = mock(NotificationRepository.class);
        var users = mock(UtilisateurRepository.class);
        var user = new Utilisateur(); user.setId(4);
        when(users.findByEmail("client@example.com")).thenReturn(Optional.of(user));
        when(notifications.findByIdAndUtilisateurId(99, 4)).thenReturn(Optional.empty());
        var service = new NotificationService(notifications, users, mock(FavoriRepository.class), mock(JdbcTemplate.class));

        assertThrows(ResponseStatusException.class, () -> service.read("client@example.com", 99));
        verify(notifications).findByIdAndUtilisateurId(99, 4);
        verify(notifications, never()).save(any());
    }

    @Test void stockNotificationsNeedARealStateTransition() {
        var jdbc = mock(JdbcTemplate.class);
        var users = mock(UtilisateurRepository.class);
        var favoris = mock(FavoriRepository.class);
        var service = new NotificationService(mock(NotificationRepository.class), users, favoris, jdbc);
        service.stockChanged(5, "Canapé", 9, "OSLO", 2, 1, 5, true, true, "movement-1");
        verifyNoInteractions(users, favoris, jdbc);
    }
}
