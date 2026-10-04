package com.amansia.amansiabackend.controller;

import com.amansia.amansiabackend.dto.variante.VarianteProduitCreationDTO;
import com.amansia.amansiabackend.dto.variante.VarianteProduitDTO;
import com.amansia.amansiabackend.dto.variante.VarianteProduitUpdateDTO;
import com.amansia.amansiabackend.service.VarianteProduitService;
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

@RestController
@RequestMapping("/api/variantes-produits")
@RequiredArgsConstructor
public class VarianteProduitController {
    private final com.amansia.amansiabackend.service.CataloguePublicService cataloguePublicService;

    private final VarianteProduitService varianteProduitService;

    @PostMapping
    public ResponseEntity<VarianteProduitDTO> creerVarianteProduit(@Valid @RequestBody VarianteProduitCreationDTO creationDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(varianteProduitService.creerVarianteProduit(creationDTO));
    }

    @GetMapping
    public ResponseEntity<List<VarianteProduitDTO>> recupererToutesLesVariantesProduit() {
        return ResponseEntity.ok(varianteProduitService.recupererToutesLesVariantesProduit());
    }

    @GetMapping("/produit/{produitId}")
    public ResponseEntity<List<VarianteProduitDTO>> recupererVariantesProduitParProduit(@PathVariable Integer produitId) {
        return ResponseEntity.ok(varianteProduitService.recupererVariantesProduitParProduit(produitId));
    }

    @GetMapping("/produit/{produitId}/actives")
    public ResponseEntity<List<VarianteProduitDTO>> recupererVariantesProduitActivesParProduit(@PathVariable Integer produitId) {
        return ResponseEntity.ok(cataloguePublicService.variantes(produitId));
    }

    @GetMapping("/recherche")
    public ResponseEntity<List<VarianteProduitDTO>> rechercherParReference(@RequestParam String reference) {
        return ResponseEntity.ok(varianteProduitService.rechercherParReference(reference));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VarianteProduitDTO> recupererVarianteProduitParId(@PathVariable Integer id) {
        return ResponseEntity.ok(varianteProduitService.recupererVarianteProduitParId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VarianteProduitDTO> modifierVarianteProduit(
            @PathVariable Integer id,
            @Valid @RequestBody VarianteProduitUpdateDTO updateDTO) {
        return ResponseEntity.ok(varianteProduitService.modifierVarianteProduit(id, updateDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactiverVarianteProduit(@PathVariable Integer id) {
        varianteProduitService.desactiverVarianteProduit(id);
        return ResponseEntity.noContent().build();
    }
}
