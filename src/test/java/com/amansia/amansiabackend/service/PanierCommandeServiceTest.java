package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.entity.*;
import com.amansia.amansiabackend.repository.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class PanierCommandeServiceTest {
    @Test void keepsAdditionalQuantitiesAndUnrelatedProducts() {
        var paniers = mock(PanierRepository.class); var lignes = mock(LignePanierRepository.class);
        var commandes = mock(LigneCommandeRepository.class);
        var service = new PanierCommandeService(paniers, lignes, commandes);
        var utilisateur = new Utilisateur(); utilisateur.setId(1);
        var commande = new Commande(); commande.setId(2); commande.setUtilisateur(utilisateur);
        var panier = new Panier(); panier.setId(3);
        var produit = new Produit(); produit.setId(4);
        var autre = new Produit(); autre.setId(5);
        var ligne = new LignePanier(); ligne.setProduit(produit); ligne.setQuantite(3);
        var intact = new LignePanier(); intact.setProduit(autre); intact.setQuantite(1);
        var achat = new LigneCommande(); achat.setProduit(produit); achat.setQuantite(2);
        when(paniers.findByUtilisateurId(1)).thenReturn(Optional.of(panier));
        when(lignes.findByPanierId(3)).thenReturn(List.of(ligne, intact));
        when(commandes.findByCommandeId(2)).thenReturn(List.of(achat));
        service.retirerArticlesCommandes(commande);
        assertEquals(1, ligne.getQuantite()); verify(lignes).save(ligne);
        verify(lignes, never()).delete(intact); verify(lignes, never()).save(intact);
        ligne.setQuantite(2);
        service.retirerArticlesCommandes(commande);
        verify(lignes).delete(ligne);
    }
}
