package com.amansia.amansiabackend.controller;

import com.amansia.amansiabackend.dto.avis.AvisCreationDTO;
import com.amansia.amansiabackend.dto.avis.AvisDTO;
import com.amansia.amansiabackend.dto.avis.AvisUpdateDTO;
import com.amansia.amansiabackend.entity.Utilisateur;
import com.amansia.amansiabackend.exception.UtilisateurNotFoundException;
import com.amansia.amansiabackend.repository.UtilisateurRepository;
import com.amansia.amansiabackend.service.AvisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/api/avis")
@RequiredArgsConstructor
public class AvisController {
    private final com.amansia.amansiabackend.service.CataloguePublicService cataloguePublicService;

    private final AvisService avisService;
    private final UtilisateurRepository utilisateurRepository;

    @PostMapping
    public ResponseEntity<AvisDTO> creerAvis(
            Authentication authentication,
            @Valid @RequestBody AvisCreationDTO creationDTO) {
        Utilisateur utilisateur = recupererUtilisateurConnecte(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(avisService.creerAvis(utilisateur, creationDTO));
    }

    @GetMapping("/produit/{produitId}")
    public ResponseEntity<List<AvisDTO>> recupererAvisProduit(
            Authentication authentication,
            @PathVariable Integer produitId) {
        if (!estAdministrateur(authentication)) cataloguePublicService.verifierProduit(produitId);
        boolean administrateur = estAdministrateur(authentication);
        Integer utilisateurId = authentication != null && !administrateur && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_CLIENT".equals(authority.getAuthority()))
                ? recupererUtilisateurConnecte(authentication).getId() : null;
        return ResponseEntity.ok(avisService.recupererAvisProduit(produitId, administrateur, utilisateurId));
    }

    @GetMapping("/admin")
    public ResponseEntity<List<AvisDTO>> recupererTousAvis() {
        return ResponseEntity.ok(avisService.recupererTousAvis());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AvisDTO> recupererAvis(Authentication authentication, @PathVariable Integer id) {
        Utilisateur utilisateur = recupererUtilisateurConnecte(authentication);
        return ResponseEntity.ok(avisService.recupererAvis(id, utilisateur, estAdministrateur(authentication)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AvisDTO> modifierAvis(
            Authentication authentication,
            @PathVariable Integer id,
            @Valid @RequestBody AvisUpdateDTO updateDTO) {
        Utilisateur utilisateur = recupererUtilisateurConnecte(authentication);
        return ResponseEntity.ok(avisService.modifierAvis(id, utilisateur, updateDTO, estAdministrateur(authentication)));
    }

    @PutMapping("/{id}/approbation")
    public ResponseEntity<AvisDTO> modererAvis(
            @PathVariable Integer id,
            @RequestParam boolean approuve) {
        return ResponseEntity.ok(avisService.modererAvis(id, approuve));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerAvis(Authentication authentication, @PathVariable Integer id) {
        Utilisateur utilisateur = recupererUtilisateurConnecte(authentication);
        avisService.supprimerAvis(id, utilisateur, estAdministrateur(authentication));
        return ResponseEntity.noContent().build();
    }

    private boolean estAdministrateur(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMINISTRATEUR".equals(authority.getAuthority()));
    }

    private Utilisateur recupererUtilisateurConnecte(Authentication authentication) {
        return utilisateurRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UtilisateurNotFoundException("email", authentication.getName()));
    }
}
