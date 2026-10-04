package com.amansia.amansiabackend.controller;

import com.amansia.amansiabackend.dto.adresse.AdresseDTO;
import com.amansia.amansiabackend.dto.adresse.AdresseRequest;
import com.amansia.amansiabackend.entity.Utilisateur;
import com.amansia.amansiabackend.exception.UtilisateurNotFoundException;
import com.amansia.amansiabackend.repository.UtilisateurRepository;
import com.amansia.amansiabackend.service.AdresseService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/adresses")
@RequiredArgsConstructor
public class AdresseController {
    private final AdresseService service;
    private final UtilisateurRepository utilisateurs;

    @GetMapping
    public List<AdresseDTO> lister(Authentication authentication) {
        return service.lister(utilisateur(authentication));
    }

    @GetMapping("/{id}")
    public AdresseDTO consulter(Authentication authentication, @PathVariable Integer id) {
        return service.consulter(utilisateur(authentication), id);
    }

    @PostMapping
    public ResponseEntity<AdresseDTO> creer(Authentication authentication, @Valid @RequestBody AdresseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(utilisateur(authentication), request));
    }

    @PutMapping("/{id}")
    public AdresseDTO modifier(Authentication authentication, @PathVariable Integer id,
            @Valid @RequestBody AdresseRequest request) {
        return service.modifier(utilisateur(authentication), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(Authentication authentication, @PathVariable Integer id) {
        service.supprimer(utilisateur(authentication), id);
        return ResponseEntity.noContent().build();
    }

    private Utilisateur utilisateur(Authentication authentication) {
        return utilisateurs.findByEmail(authentication.getName())
                .orElseThrow(() -> new UtilisateurNotFoundException("email", authentication.getName()));
    }
}
