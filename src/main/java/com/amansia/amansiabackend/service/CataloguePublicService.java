package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.image.ImageProduitDTO;
import com.amansia.amansiabackend.dto.produit.ProduitDTO;
import com.amansia.amansiabackend.dto.produit.BornesPrixCatalogueDTO;
import com.amansia.amansiabackend.dto.variante.VarianteProduitDTO;
import com.amansia.amansiabackend.entity.Produit;
import com.amansia.amansiabackend.entity.VarianteProduit;
import com.amansia.amansiabackend.exception.ImageProduitNotFoundException;
import com.amansia.amansiabackend.exception.ProduitNotFoundException;
import com.amansia.amansiabackend.repository.ImageProduitRepository;
import com.amansia.amansiabackend.repository.ProduitRepository;
import com.amansia.amansiabackend.repository.VarianteProduitRepository;
import java.util.List;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CataloguePublicService {
    private final ProduitRepository produits;
    private final ImageProduitRepository images;
    private final VarianteProduitRepository variantes;
    private final ProduitService produitService;
    private final ImageProduitService imageService;
    private final VarianteProduitService varianteService;
    private final StockService stockService;

    public static boolean visible(Produit produit) {
        return produit != null && Boolean.TRUE.equals(produit.getActif())
                && produit.getCategorie() != null
                && Boolean.TRUE.equals(produit.getCategorie().getActif());
    }

    public boolean produitVisible(Integer id) {
        return produits.findById(id).map(CataloguePublicService::visible).orElse(false);
    }

    public void verifierProduit(Integer id) {
        if (!produitVisible(id)) {
            throw new ProduitNotFoundException(id);
        }
    }

    public ProduitDTO produit(Integer id) {
        verifierProduit(id);
        ProduitDTO dto = produitService.recupererProduitParId(id);
        dto.setDisponible(stockService.disponibiliteProduits().getOrDefault(id,0)>0);
        return dto;
    }

    public List<ProduitDTO> produits() {
        return filtrer(produitService.recupererProduitsActifs());
    }

    public List<ProduitDTO> catalogue(String nom, Integer categorieId, BigDecimal prixMin, BigDecimal prixMax) {
        var disponibles = stockService.disponibiliteProduits();
        return produitService.rechercherCatalogue(nom, categorieId, prixMin, prixMax).stream()
                .peek(produit -> produit.setDisponible(disponibles.getOrDefault(produit.getId(), 0) > 0))
                .toList();
    }

    public BornesPrixCatalogueDTO bornesPrix() {
        var bornes = produits.bornesPrixCatalogue();
        return new BornesPrixCatalogueDTO(bornes.getMinimum(), bornes.getMaximum());
    }

    public List<ProduitDTO> recherche(String nom) {
        return filtrer(produitService.rechercherParNom(nom));
    }

    public List<ProduitDTO> categorie(Integer id) {
        return filtrer(produitService.recupererProduitsActifsParCategorie(id));
    }

    private List<ProduitDTO> filtrer(List<ProduitDTO> liste) {
        var disponibles=stockService.disponibiliteProduits();
        return liste.stream().filter(produit -> produitVisible(produit.getId())).peek(produit -> produit.setDisponible(disponibles.getOrDefault(produit.getId(),0)>0)).toList();
    }

    public List<ImageProduitDTO> images() {
        return imageService.recupererToutesLesImages().stream()
                .filter(image -> produitVisible(image.getProduitId())).toList();
    }

    public List<ImageProduitDTO> imagesProduit(Integer id) {
        verifierProduit(id);
        return imageService.recupererImagesParProduit(id);
    }

    public ImageProduitDTO image(Integer id) {
        ImageProduitDTO image = imageService.recupererImageParId(id);
        if (!produitVisible(image.getProduitId())) {
            throw new ImageProduitNotFoundException(id);
        }
        return image;
    }

    public boolean fichierVisible(String url) {
        return images.findByUrl(url).stream().anyMatch(image -> visible(image.getProduit()));
    }

    public List<VarianteProduitDTO> variantes(Integer id) {
        verifierProduit(id);
        var quantites=stockService.stocksProduit(id).stream().collect(java.util.stream.Collectors.toMap(com.amansia.amansiabackend.dto.stock.StockDTO::varianteId,com.amansia.amansiabackend.dto.stock.StockDTO::quantite));
        return varianteService.recupererVariantesProduitActivesParProduit(id).stream()
                .filter(variante -> variantes.findById(variante.getId()).map(this::visible).orElse(false))
                .peek(variante -> variante.setQuantiteDisponible(quantites.getOrDefault(variante.getId(),0)))
                .toList();
    }

    private boolean visible(VarianteProduit variante) {
        return Boolean.TRUE.equals(variante.getActif()) && visible(variante.getProduit())
                && (variante.getMateriau() == null || Boolean.TRUE.equals(variante.getMateriau().getActif()))
                && (variante.getCouleur() == null || Boolean.TRUE.equals(variante.getCouleur().getActif()));
    }
}
