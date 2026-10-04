package com.amansia.amansiabackend.controller;

import com.amansia.amansiabackend.dto.couleur.CouleurCreationDTO;
import com.amansia.amansiabackend.dto.couleur.CouleurDTO;
import com.amansia.amansiabackend.dto.couleur.CouleurUpdateDTO;
import com.amansia.amansiabackend.service.CouleurService;
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
@RequestMapping("/api/couleurs")
@RequiredArgsConstructor
public class CouleurController {

    private final CouleurService couleurService;

    @PostMapping
    public ResponseEntity<CouleurDTO> creerCouleur(@Valid @RequestBody CouleurCreationDTO creationDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(couleurService.creerCouleur(creationDTO));
    }

    @GetMapping
    public ResponseEntity<List<CouleurDTO>> recupererToutesLesCouleurs() {
        return ResponseEntity.ok(couleurService.recupererToutesLesCouleurs());
    }

    @GetMapping("/actives")
    public ResponseEntity<List<CouleurDTO>> recupererCouleursActives() {
        return ResponseEntity.ok(couleurService.recupererCouleursActives());
    }

    @GetMapping("/recherche")
    public ResponseEntity<List<CouleurDTO>> rechercherParNom(@RequestParam String nom) {
        return ResponseEntity.ok(couleurService.rechercherParNom(nom));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CouleurDTO> recupererCouleurParId(@PathVariable Integer id) {
        return ResponseEntity.ok(couleurService.recupererCouleurParId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CouleurDTO> modifierCouleur(
            @PathVariable Integer id,
            @Valid @RequestBody CouleurUpdateDTO updateDTO) {
        return ResponseEntity.ok(couleurService.modifierCouleur(id, updateDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactiverCouleur(@PathVariable Integer id) {
        couleurService.desactiverCouleur(id);
        return ResponseEntity.noContent().build();
    }
}
