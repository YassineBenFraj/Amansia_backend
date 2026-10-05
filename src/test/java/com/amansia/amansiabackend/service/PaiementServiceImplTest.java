package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.paiement.PaiementCreationDTO;
import com.amansia.amansiabackend.dto.paiement.PaiementDTO;
import com.amansia.amansiabackend.entity.Commande;
import com.amansia.amansiabackend.entity.MethodePaiement;
import com.amansia.amansiabackend.entity.StatutCommande;
import com.amansia.amansiabackend.entity.StatutPaiement;
import com.amansia.amansiabackend.entity.Utilisateur;
import com.amansia.amansiabackend.exception.CommandeAccessDeniedException;
import com.amansia.amansiabackend.repository.CommandeRepository;
import com.amansia.amansiabackend.repository.PaiementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaiementServiceImplTest {

    @Mock
    private PaiementRepository paiementRepository;

    @Mock
    private CommandeRepository commandeRepository;

    @Mock
    private StripePaymentService stripePaymentService;
    @Mock private PanierCommandeService panierCommandeService;
    @Mock private NotificationService notifications;

    @InjectMocks
    private PaiementServiceImpl paiementService;

    @Test
    void creerPaiement_shouldUseOrderTotalAndRemainPending() {
        Utilisateur utilisateur = utilisateur(1);
        Commande commande = commande(15, utilisateur);
        PaiementCreationDTO creationDTO = new PaiementCreationDTO();
        creationDTO.setCommandeId(15);
        creationDTO.setMethode(MethodePaiement.PAIEMENT_LIVRAISON);

        when(commandeRepository.findByIdForUpdate(15)).thenReturn(Optional.of(commande));
        when(paiementRepository.findByCommandeId(15)).thenReturn(List.of());
        when(paiementRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));

        PaiementDTO paiement = paiementService.creerPaiement(utilisateur, creationDTO);

        assertEquals(new BigDecimal("125.50"), paiement.getMontant());
        assertEquals(StatutPaiement.EN_ATTENTE, paiement.getStatut());
        assertEquals(MethodePaiement.PAIEMENT_LIVRAISON, paiement.getMethode());
        assertNotNull(paiement.getReference());
        verify(paiementRepository).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void creerPaiement_shouldRejectAnotherUsersOrder() {
        Utilisateur proprietaire = utilisateur(1);
        Utilisateur autreUtilisateur = utilisateur(2);
        Commande commande = commande(15, proprietaire);
        PaiementCreationDTO creationDTO = new PaiementCreationDTO();
        creationDTO.setCommandeId(15);
        creationDTO.setMethode(MethodePaiement.CARTE);

        when(commandeRepository.findByIdForUpdate(15)).thenReturn(Optional.of(commande));

        assertThrows(CommandeAccessDeniedException.class,
                () -> paiementService.creerPaiement(autreUtilisateur, creationDTO));
    }

    @Test
    void creerPaiementByCard_shouldReturnClientSecret() {
        Utilisateur utilisateur = utilisateur(1);
        Commande commande = commande(15, utilisateur);
        PaiementCreationDTO creationDTO = new PaiementCreationDTO();
        creationDTO.setCommandeId(15);
        creationDTO.setMethode(MethodePaiement.CARTE);

        when(commandeRepository.findByIdForUpdate(15)).thenReturn(Optional.of(commande));
        when(paiementRepository.findByCommandeId(15)).thenReturn(List.of());
        when(paiementRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(stripePaymentService.creerPaymentIntent(org.mockito.ArgumentMatchers.any())).thenReturn("pi_secret_test");

        PaiementDTO paiement = paiementService.creerPaiement(utilisateur, creationDTO);

        assertEquals("pi_secret_test", paiement.getClientSecret());
        assertEquals(StatutPaiement.EN_ATTENTE, paiement.getStatut());
    }

    private Utilisateur utilisateur(Integer id) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(id);
        return utilisateur;
    }

    private Commande commande(Integer id, Utilisateur utilisateur) {
        Commande commande = new Commande();
        commande.setId(id);
        commande.setUtilisateur(utilisateur);
        commande.setTotal(new BigDecimal("125.50"));
        commande.setStatut(StatutCommande.EN_ATTENTE);
        return commande;
    }
}
