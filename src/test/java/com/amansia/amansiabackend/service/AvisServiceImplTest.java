package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.avis.AvisCreationDTO;
import com.amansia.amansiabackend.dto.avis.AvisDTO;
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

    private Utilisateur utilisateur(Integer id) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(id);
        return utilisateur;
    }
}
