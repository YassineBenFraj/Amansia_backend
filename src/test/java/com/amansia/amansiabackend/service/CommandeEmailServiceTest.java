package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.auth.service.EmailService;
import com.amansia.amansiabackend.entity.Commande;
import com.amansia.amansiabackend.entity.StatutCommande;
import com.amansia.amansiabackend.entity.Utilisateur;
import com.amansia.amansiabackend.repository.AdresseRepository;
import com.amansia.amansiabackend.repository.LigneCommandeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class CommandeEmailServiceTest {
    private final EmailService email = mock(EmailService.class);
    private final CommandeEmailService service = new CommandeEmailService(email,
            mock(LigneCommandeRepository.class), mock(AdresseRepository.class));

    @BeforeEach void begin() {
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
    }

    @AfterEach void end() {
        TransactionSynchronizationManager.clearSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    @Test void emailIsSentOnlyAfterCommitAndEscapesCustomerName() {
        service.statutModifie(commande(StatutCommande.EXPEDIEE));
        verify(email, never()).envoyerTransactionnel(anyString(), anyString(), anyString(), anyString());
        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        verify(email).envoyerTransactionnel(eq("client@example.com"), contains("expédiée"),
                contains("<script>"), contains("&lt;script&gt;"));
    }

    @Test void noEmailForPreparation() {
        service.statutModifie(commande(StatutCommande.EN_PREPARATION));
        verify(email, never()).envoyerTransactionnel(anyString(), anyString(), anyString(), anyString());
    }

    private Commande commande(StatutCommande statut) {
        Utilisateur client = new Utilisateur();
        client.setPrenom("<script>"); client.setNom("Client"); client.setEmail("client@example.com");
        Commande commande = new Commande();
        commande.setId(10); commande.setNumeroCommande("CMD-10"); commande.setUtilisateur(client); commande.setStatut(statut);
        return commande;
    }
}
