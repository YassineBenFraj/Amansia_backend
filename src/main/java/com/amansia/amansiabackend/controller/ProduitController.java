package com.amansia.amansiabackend.controller;

import com.amansia.amansiabackend.dto.produit.ProduitCreationDTO;
import com.amansia.amansiabackend.dto.produit.ProduitDTO;
import com.amansia.amansiabackend.dto.produit.BornesPrixCatalogueDTO;
import com.amansia.amansiabackend.dto.produit.ProduitUpdateDTO;
import com.amansia.amansiabackend.service.ProduitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/produits")
@RequiredArgsConstructor
public class ProduitController {
    private final com.amansia.amansiabackend.service.CataloguePublicService cataloguePublicService;

    private final ProduitService produitService;
    private final com.amansia.amansiabackend.service.ProduitsSimilairesService similairesService;

    @PostMapping
    public ResponseEntity<ProduitDTO> creerProduit(@Valid @RequestBody ProduitCreationDTO creationDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(produitService.creerProduit(creationDTO));
    }

    @GetMapping
    public ResponseEntity<List<ProduitDTO>> recupererProduitsActifs(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) Integer categorie,
            @RequestParam(required = false) BigDecimal prixMin,
            @RequestParam(required = false) BigDecimal prixMax) {
        return ResponseEntity.ok(cataloguePublicService.catalogue(nom, categorie, prixMin, prixMax));
    }

    @GetMapping("/bornes-prix")
    public ResponseEntity<BornesPrixCatalogueDTO> bornesPrixCatalogue() {
        return ResponseEntity.ok(cataloguePublicService.bornesPrix());
    }

    @GetMapping("/admin")
    public ResponseEntity<List<ProduitDTO>> recupererTousLesProduits() {
        return ResponseEntity.ok(produitService.recupererTousLesProduits());
    }

    @GetMapping("/categorie/{categorieId}")
    public ResponseEntity<List<ProduitDTO>> recupererProduitsParCategorie(@PathVariable Integer categorieId) {
        return ResponseEntity.ok(produitService.recupererProduitsParCategorie(categorieId));
    }

    @GetMapping("/categorie/{categorieId}/actifs")
    public ResponseEntity<List<ProduitDTO>> recupererProduitsActifsParCategorie(@PathVariable Integer categorieId) {
        return ResponseEntity.ok(cataloguePublicService.categorie(categorieId));
    }

    @GetMapping("/recherche")
    public ResponseEntity<List<ProduitDTO>> rechercherParNom(@RequestParam String nom) {
        return ResponseEntity.ok(cataloguePublicService.recherche(nom));
    }

    @GetMapping("/{id}/similaires")
    public ResponseEntity<List<com.amansia.amansiabackend.dto.produit.ProduitSimilaireDTO>> similaires(@PathVariable Integer id) {
        return ResponseEntity.ok(similairesService.similaires(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProduitDTO> recupererProduitParId(@PathVariable Integer id, org.springframework.security.core.Authentication authentication) {
        boolean admin = authentication != null && authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMINISTRATEUR".equals(a.getAuthority()));
        return ResponseEntity.ok(admin ? produitService.recupererProduitParId(id) : cataloguePublicService.produit(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProduitDTO> modifierProduit(
            @PathVariable Integer id,
            @Valid @RequestBody ProduitUpdateDTO updateDTO) {
        return ResponseEntity.ok(produitService.modifierProduit(id, updateDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactiverProduit(@PathVariable Integer id) {
        produitService.desactiverProduit(id);
        return ResponseEntity.noContent().build();
    }
}
