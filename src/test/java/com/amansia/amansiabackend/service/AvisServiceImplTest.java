package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.avis.AvisCreationDTO;
import com.amansia.amansiabackend.dto.avis.AvisDTO;
import com.amansia.amansiabackend.dto.avis.AvisUpdateDTO;
import com.amansia.amansiabackend.entity.Avis;
import com.amansia.amansiabackend.entity.Commande;
import com.amansia.amansiabackend.entity.Produit;
import com.amansia.amansiabackend.entity.Utilisateur;
import com.amansia.amansiabackend.repository.AvisRepository;
import com.amansia.amansiabackend.repository.CommandeRepository;
import com.amansia.amansiabackend.repository.LigneCommandeRepository;
import com.amansia.amansiabackend.repository.ProduitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvisServiceImplTest {

    @Mock
    private AvisRepository avisRepository;

    @Mock
    private ProduitRepository produitRepository;

    @Mock
    private CommandeRepository commandeRepository;

    @Mock
    private LigneCommandeRepository ligneCommandeRepository;

    @Mock private NotificationService notifications;
    @InjectMocks
    private AvisServiceImpl avisService;

    @Test
    void creerAvis_shouldRequirePurchasedProductAndStartUnapproved() {
        Utilisateur utilisateur = utilisateur(1);
        Produit produit = new Produit();
        produit.setId(5);
        Commande commande = new Commande();
        commande.setId(20);
        commande.setUtilisateur(utilisateur);
        AvisCreationDTO dto = new AvisCreationDTO();
        dto.setProduitId(5);
        dto.setCommandeId(20);
        dto.setNote(5);
        dto.setCommentaire("Très bon produit.");

        when(produitRepository.findById(5)).thenReturn(Optional.of(produit));
        when(commandeRepository.findByIdForUpdate(20)).thenReturn(Optional.of(commande));
        when(ligneCommandeRepository.existsByCommandeIdAndProduitId(20, 5)).thenReturn(true);
        when(avisRepository.existsByUtilisateurIdAndProduitIdAndCommandeId(1, 5, 20)).thenReturn(false);
        when(avisRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AvisDTO avis = avisService.creerAvis(utilisateur, dto);

        assertEquals(5, avis.getNote());
        assertFalse(avis.getApprouve());
        verify(ligneCommandeRepository).existsByCommandeIdAndProduitId(20, 5);
    }

    @Test
    void creerAvis_shouldRejectNoteOutsideRange() {
        AvisCreationDTO dto = new AvisCreationDTO();
        dto.setProduitId(5);
        dto.setCommandeId(20);
        dto.setNote(6);

        assertThrows(IllegalArgumentException.class, () -> avisService.creerAvis(utilisateur(1), dto));
    }

    @Test
    void clientSeesOwnPendingReviewButVisitorOnlySeesApprovedReviews() {
        Avis approved = review(1, 2, true);
        Avis pending = review(2, 1, false);
        when(produitRepository.existsById(5)).thenReturn(true);
        when(avisRepository.findPublicAndOwnedByProduct(5, 1)).thenReturn(List.of(approved, pending));
        when(avisRepository.findByProduitIdAndApprouveTrueOrderByDateCreationDesc(5)).thenReturn(List.of(approved));

        assertEquals(2, avisService.recupererAvisProduit(5, false, 1).size());
        assertEquals(1, avisService.recupererAvisProduit(5, false, null).size());
    }

    @Test
    void clientEditReturnsToModerationAndNotifiesAdminOnce() {
        Avis avis = review(8, 1, true);
        avis.setCommentaire("Ancien commentaire");
        AvisUpdateDTO dto = new AvisUpdateDTO();
        dto.setNote(4);
        dto.setCommentaire("Nouveau commentaire");
        when(avisRepository.findById(8)).thenReturn(Optional.of(avis));
        when(avisRepository.save(avis)).thenReturn(avis);

        AvisDTO updated = avisService.modifierAvis(8, avis.getUtilisateur(), dto, false);
        assertFalse(updated.getApprouve());
        assertEquals("Nouveau commentaire", updated.getCommentaire());
        verify(notifications).reviewUpdated(8, avis.getProduit().getNom());

        avisService.modifierAvis(8, avis.getUtilisateur(), dto, false);
        verify(avisRepository).save(avis);
        verify(notifications).reviewUpdated(8, avis.getProduit().getNom());
    }

    private Avis review(Integer id, Integer userId, boolean approved) {
        Avis avis = new Avis();
        avis.setId(id);
        avis.setUtilisateur(utilisateur(userId));
        Produit produit = new Produit();
        produit.setId(5);
        produit.setNom("Table");
        avis.setProduit(produit);
        avis.setNote((byte) 4);
        avis.setApprouve(approved);
        return avis;
    }

    private Utilisateur utilisateur(Integer id) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(id);
        return utilisateur;
    }
}
