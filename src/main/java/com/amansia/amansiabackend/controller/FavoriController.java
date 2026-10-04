package com.amansia.amansiabackend.controller;

import com.amansia.amansiabackend.dto.favori.FavoriDTO;
import com.amansia.amansiabackend.entity.Utilisateur;
import com.amansia.amansiabackend.exception.UtilisateurNotFoundException;
import com.amansia.amansiabackend.repository.UtilisateurRepository;
import com.amansia.amansiabackend.service.FavoriService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favoris")
@RequiredArgsConstructor
public class FavoriController {
    private final FavoriService service;
    private final UtilisateurRepository utilisateurs;

    @GetMapping
    public List<FavoriDTO> lister(Authentication authentication) {
        return service.lister(utilisateur(authentication));
    }

    @GetMapping("/{produitId}/existe")
    public boolean existe(Authentication authentication, @PathVariable Integer produitId) {
        return service.existe(utilisateur(authentication), produitId);
    }

    @PostMapping("/{produitId}")
    public FavoriDTO ajouter(Authentication authentication, @PathVariable Integer produitId) {
        return service.ajouter(utilisateur(authentication), produitId);
    }

    @DeleteMapping("/{produitId}")
    public ResponseEntity<Void> retirer(Authentication authentication, @PathVariable Integer produitId) {
        service.retirer(utilisateur(authentication), produitId);
        return ResponseEntity.noContent().build();
    }

    private Utilisateur utilisateur(Authentication authentication) {
        return utilisateurs.findByEmail(authentication.getName())
                .orElseThrow(() -> new UtilisateurNotFoundException("email", authentication.getName()));
    }
}
