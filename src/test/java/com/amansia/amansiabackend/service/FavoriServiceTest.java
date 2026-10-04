package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.entity.Favori;
import com.amansia.amansiabackend.entity.Produit;
import com.amansia.amansiabackend.entity.Utilisateur;
import com.amansia.amansiabackend.exception.ProduitNotFoundException;
import com.amansia.amansiabackend.repository.FavoriRepository;
import com.amansia.amansiabackend.repository.ImageProduitRepository;
import com.amansia.amansiabackend.repository.ProduitRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class FavoriServiceTest {
    @Mock FavoriRepository favoris;
    @Mock ProduitRepository produits;
    @Mock ImageProduitRepository images;
    @InjectMocks FavoriService service;

    @Test
    void duplicateAdditionReturnsExistingFavoriWithoutSaving() {
        Utilisateur client = client(7);
        Produit produit = new Produit();
        produit.setId(9);
        produit.setNom("Chaise");
        Favori existing = new Favori();
        existing.setProduit(produit);
        existing.setUtilisateur(client);
        when(favoris.findByUtilisateurIdAndProduitId(7, 9)).thenReturn(Optional.of(existing));
        when(images.findByProduitIdOrderByOrdreAsc(9)).thenReturn(List.of());

        assertEquals(9, service.ajouter(client, 9).produitId());
        verify(favoris, never()).saveAndFlush(any());
    }

    @Test
    void deletionIsScopedToAuthenticatedClient() {
        Utilisateur client = client(7);
        when(favoris.findByUtilisateurIdAndProduitId(7, 9)).thenReturn(Optional.empty());

        service.retirer(client, 9);

        verify(favoris).findByUtilisateurIdAndProduitId(7, 9);
        verify(favoris, never()).delete(any());
    }

    @Test
    void invalidAndMissingProductCannotBeAdded() {
        Utilisateur client = client(7);
        assertThrows(ResponseStatusException.class, () -> service.ajouter(client, 0));
        when(favoris.findByUtilisateurIdAndProduitId(7, 9)).thenReturn(Optional.empty());
        when(produits.findById(9)).thenReturn(Optional.empty());
        assertThrows(ProduitNotFoundException.class, () -> service.ajouter(client, 9));
    }

    private static Utilisateur client(int id) {
        Utilisateur client = new Utilisateur();
        client.setId(id);
        return client;
    }
}
