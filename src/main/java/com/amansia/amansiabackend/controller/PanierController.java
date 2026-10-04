package com.amansia.amansiabackend.controller;

import com.amansia.amansiabackend.dto.panier.LignePanierCreationDTO;
import com.amansia.amansiabackend.dto.panier.LignePanierUpdateDTO;
import com.amansia.amansiabackend.dto.panier.PanierDTO;
import com.amansia.amansiabackend.entity.Utilisateur;
import com.amansia.amansiabackend.exception.UtilisateurNotFoundException;
import com.amansia.amansiabackend.repository.UtilisateurRepository;
import com.amansia.amansiabackend.service.PanierService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/panier")
@RequiredArgsConstructor
public class PanierController {

    private final PanierService panierService;
    private final UtilisateurRepository utilisateurRepository;

    @GetMapping
    public ResponseEntity<PanierDTO> recupererPanier(Authentication authentication) {
        Utilisateur utilisateur = recupererUtilisateurConnecte(authentication);
        return ResponseEntity.ok(panierService.obtenirPanier(utilisateur));
    }

    @PostMapping("/lignes")
    public ResponseEntity<PanierDTO> ajouterLignePanier(
            Authentication authentication,
            @Valid @RequestBody LignePanierCreationDTO creationDTO) {
        Utilisateur utilisateur = recupererUtilisateurConnecte(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(panierService.ajouterLignePanier(utilisateur, creationDTO));
    }

    @PutMapping("/lignes/{ligneId}")
    public ResponseEntity<PanierDTO> modifierLignePanier(
            Authentication authentication,
            @PathVariable Integer ligneId,
            @Valid @RequestBody LignePanierUpdateDTO updateDTO) {
        Utilisateur utilisateur = recupererUtilisateurConnecte(authentication);
        return ResponseEntity.ok(panierService.modifierLignePanier(utilisateur, ligneId, updateDTO));
    }

    @DeleteMapping("/lignes/{ligneId}")
    public ResponseEntity<Void> supprimerLignePanier(Authentication authentication, @PathVariable Integer ligneId) {
        Utilisateur utilisateur = recupererUtilisateurConnecte(authentication);
        panierService.supprimerLignePanier(utilisateur, ligneId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> viderPanier(Authentication authentication) {
        Utilisateur utilisateur = recupererUtilisateurConnecte(authentication);
        panierService.viderPanier(utilisateur);
        return ResponseEntity.noContent().build();
    }

    private Utilisateur recupererUtilisateurConnecte(Authentication authentication) {
        return utilisateurRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UtilisateurNotFoundException("email", authentication.getName()));
    }
}
