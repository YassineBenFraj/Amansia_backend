package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.panier.LignePanierCreationDTO;
import com.amansia.amansiabackend.entity.Produit;
import com.amansia.amansiabackend.entity.Utilisateur;
import com.amansia.amansiabackend.exception.ProduitInactiveException;
import com.amansia.amansiabackend.repository.LignePanierRepository;
import com.amansia.amansiabackend.repository.PanierRepository;
import com.amansia.amansiabackend.repository.ProduitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PanierServiceImplTest {

    @Mock
    private PanierRepository panierRepository;

    @Mock
    private LignePanierRepository lignePanierRepository;

    @Mock
    private ProduitRepository produitRepository;

    @InjectMocks
    private PanierServiceImpl panierService;

    @Test
    void ajouterLignePanier_shouldRejectInactiveProduct() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(1);

        LignePanierCreationDTO dto = new LignePanierCreationDTO();
        dto.setProduitId(10);
        dto.setQuantite(1);

        Produit produit = new Produit();
        produit.setId(10);
        produit.setActif(false);

        when(produitRepository.findById(10)).thenReturn(Optional.of(produit));

        assertThrows(ProduitInactiveException.class, () -> panierService.ajouterLignePanier(utilisateur, dto));
    }
}
