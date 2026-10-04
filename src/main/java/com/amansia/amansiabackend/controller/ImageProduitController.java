package com.amansia.amansiabackend.controller;

import com.amansia.amansiabackend.dto.image.ImageProduitDTO;
import com.amansia.amansiabackend.dto.image.ImageProduitUpdateDTO;
import com.amansia.amansiabackend.service.ImageProduitService;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/images-produits")
@RequiredArgsConstructor
public class ImageProduitController {
    private final com.amansia.amansiabackend.service.CataloguePublicService cataloguePublicService;

    private final ImageProduitService imageProduitService;

    @PostMapping("/upload")
    public ResponseEntity<ImageProduitDTO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam Integer produitId,
            @RequestParam(defaultValue = "false") Boolean principale,
            @RequestParam(defaultValue = "0") Integer ordre) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(imageProduitService.upload(file, produitId, principale, ordre));
    }

    @GetMapping
    public ResponseEntity<List<ImageProduitDTO>> recupererToutesLesImages(org.springframework.security.core.Authentication authentication) {
        return ResponseEntity.ok(estAdministrateur(authentication) ? imageProduitService.recupererToutesLesImages() : cataloguePublicService.images());
    }

    @GetMapping("/produit/{produitId}")
    public ResponseEntity<List<ImageProduitDTO>> recupererImagesParProduit(@PathVariable Integer produitId, org.springframework.security.core.Authentication authentication) {
        return ResponseEntity.ok(estAdministrateur(authentication) ? imageProduitService.recupererImagesParProduit(produitId) : cataloguePublicService.imagesProduit(produitId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImageProduitDTO> recupererImageParId(@PathVariable Integer id, org.springframework.security.core.Authentication authentication) {
        return ResponseEntity.ok(estAdministrateur(authentication) ? imageProduitService.recupererImageParId(id) : cataloguePublicService.image(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ImageProduitDTO> modifierImage(
            @PathVariable Integer id,
            @Valid @RequestBody ImageProduitUpdateDTO updateDTO) {
        return ResponseEntity.ok(imageProduitService.modifierImage(id, updateDTO));
    }

    @PutMapping("/{id}/upload")
    public ResponseEntity<ImageProduitDTO> remplacerFichier(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(imageProduitService.remplacerFichier(id, file));
    }

    @PutMapping("/{id}/principale")
    public ResponseEntity<ImageProduitDTO> definirCommePrincipale(@PathVariable Integer id) {
        return ResponseEntity.ok(imageProduitService.definirCommePrincipale(id));
    }

    private boolean estAdministrateur(org.springframework.security.core.Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMINISTRATEUR".equals(authority.getAuthority()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerImage(@PathVariable Integer id) {
        imageProduitService.supprimerImage(id);
        return ResponseEntity.noContent().build();
    }
}
