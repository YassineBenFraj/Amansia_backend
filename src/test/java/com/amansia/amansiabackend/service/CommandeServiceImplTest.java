package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.commande.CommandeCreationDTO;
import com.amansia.amansiabackend.entity.LignePanier;
import com.amansia.amansiabackend.entity.Panier;
import com.amansia.amansiabackend.entity.Produit;
import com.amansia.amansiabackend.entity.Utilisateur;
import com.amansia.amansiabackend.exception.ProduitInactiveException;
import com.amansia.amansiabackend.repository.CommandeRepository;
import com.amansia.amansiabackend.repository.LigneCommandeRepository;
import com.amansia.amansiabackend.repository.LignePanierRepository;
import com.amansia.amansiabackend.repository.PanierRepository;
import com.amansia.amansiabackend.repository.ProduitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommandeServiceImplTest {

    @Mock
    private CommandeRepository commandeRepository;

    @Mock
    private LigneCommandeRepository ligneCommandeRepository;

    @Mock
    private PanierRepository panierRepository;

    @Mock
    private LignePanierRepository lignePanierRepository;

    @Mock
    private ProduitRepository produitRepository;

    @Mock private AdresseService adresseService;
    @Mock private com.amansia.amansiabackend.repository.PaiementRepository paiementRepository;
    @Mock private com.amansia.amansiabackend.repository.AdresseRepository adresseRepository;
    @InjectMocks
    private CommandeServiceImpl commandeService;

    @Test
    void creerCommande_shouldRejectInactiveProductInPanier() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(1);

        Panier panier = new Panier();
        panier.setId(5);
        panier.setUtilisateur(utilisateur);

        Produit produit = new Produit();
        produit.setId(10);
        produit.setActif(false);
        produit.setPrix(BigDecimal.valueOf(150.00));

        LignePanier lignePanier = new LignePanier();
        lignePanier.setId(1);
        lignePanier.setPanier(panier);
        lignePanier.setProduit(produit);
        lignePanier.setQuantite(1);
        lignePanier.setPrixUnitaire(BigDecimal.valueOf(150.00));

        CommandeCreationDTO dto = new CommandeCreationDTO();
        dto.setAdresseLivraisonId(1);

        when(panierRepository.findByUtilisateurId(1)).thenReturn(Optional.of(panier));
        when(lignePanierRepository.findByPanierId(5)).thenReturn(List.of(lignePanier));
        when(produitRepository.findById(10)).thenReturn(Optional.of(produit));

        assertThrows(ProduitInactiveException.class, () -> commandeService.creerCommande(utilisateur, dto));
    }
}
