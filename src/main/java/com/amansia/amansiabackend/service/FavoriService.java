package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.favori.FavoriDTO;
import com.amansia.amansiabackend.entity.Favori;
import com.amansia.amansiabackend.entity.ImageProduit;
import com.amansia.amansiabackend.entity.Produit;
import com.amansia.amansiabackend.entity.Utilisateur;
import com.amansia.amansiabackend.exception.ProduitNotFoundException;
import com.amansia.amansiabackend.repository.FavoriRepository;
import com.amansia.amansiabackend.repository.ImageProduitRepository;
import com.amansia.amansiabackend.repository.ProduitRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class FavoriService {
    private final FavoriRepository favoris;
    private final ProduitRepository produits;
    private final ImageProduitRepository images;

    @Transactional(readOnly = true)
    public List<FavoriDTO> lister(Utilisateur utilisateur) {
        return favoris.findByUtilisateurIdOrderByDateCreationDesc(utilisateur.getId())
                .stream().map(this::dto).toList();
    }

    @Transactional(readOnly = true)
    public boolean existe(Utilisateur utilisateur, Integer produitId) {
        verifierId(produitId);
        return favoris.existsByUtilisateurIdAndProduitId(utilisateur.getId(), produitId);
    }

    public FavoriDTO ajouter(Utilisateur utilisateur, Integer produitId) {
        verifierId(produitId);
        Favori existant = favoris.findByUtilisateurIdAndProduitId(utilisateur.getId(), produitId).orElse(null);
        if (existant != null) return dto(existant);
        Produit produit = produits.findById(produitId)
                .filter(CataloguePublicService::visible)
                .orElseThrow(() -> new ProduitNotFoundException(produitId));
        Favori favori = new Favori();
        favori.setUtilisateur(utilisateur);
        favori.setProduit(produit);
        try {
            return dto(favoris.saveAndFlush(favori));
        } catch (DataIntegrityViolationException exception) {
            // La contrainte UNIQUE existante protège aussi les requêtes concurrentes.
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Produit déjà dans les favoris", exception);
        }
    }

    public void retirer(Utilisateur utilisateur, Integer produitId) {
        verifierId(produitId);
        favoris.findByUtilisateurIdAndProduitId(utilisateur.getId(), produitId)
                .ifPresent(favoris::delete);
    }

    private void verifierId(Integer produitId) {
        if (produitId == null || produitId < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Identifiant de produit invalide");
        }
    }

    private FavoriDTO dto(Favori favori) {
        Produit produit = favori.getProduit();
        String image = images.findByProduitIdOrderByOrdreAsc(produit.getId()).stream()
                .min(Comparator.comparing((ImageProduit item) -> !Boolean.TRUE.equals(item.getPrincipale()))
                        .thenComparing(ImageProduit::getOrdre))
                .map(ImageProduit::getUrl).orElse(null);
        return new FavoriDTO(favori.getId(), produit.getId(), produit.getNom(),
                produit.getPrix(), produit.getPrixPromotion(), image,
                CataloguePublicService.visible(produit), favori.getDateCreation());
    }
}
