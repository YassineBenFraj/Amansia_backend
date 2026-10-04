package com.amansia.amansiabackend.controller;

import com.amansia.amansiabackend.dto.UtilisateurCreationDTO;
import com.amansia.amansiabackend.dto.UtilisateurDTO;
import com.amansia.amansiabackend.dto.UtilisateurUpdateDTO;
import com.amansia.amansiabackend.dto.UtilisateurStatutDTO;
import com.amansia.amansiabackend.service.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @PostMapping
    public ResponseEntity<UtilisateurDTO> creerUtilisateur(@Valid @RequestBody UtilisateurCreationDTO utilisateurCreationDTO) {
        UtilisateurDTO utilisateurDTO = utilisateurService.creerUtilisateur(utilisateurCreationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(utilisateurDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurDTO> recupererUtilisateurParId(@PathVariable Integer id) {
        UtilisateurDTO utilisateurDTO = utilisateurService.recupererUtilisateurParId(id);
        return ResponseEntity.ok(utilisateurDTO);
    }

    @GetMapping
    public ResponseEntity<List<UtilisateurDTO>> recupererTousLesUtilisateurs() {
        List<UtilisateurDTO> utilisateurs = utilisateurService.recupererTousLesUtilisateurs();
        return ResponseEntity.ok(utilisateurs);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UtilisateurDTO> modifierUtilisateur(Authentication authentication, @PathVariable Integer id, @Valid @RequestBody UtilisateurUpdateDTO utilisateurUpdateDTO) {
        UtilisateurDTO utilisateurDTO = utilisateurService.modifierUtilisateur(id, utilisateurUpdateDTO, authentication.getName());
        return ResponseEntity.ok(utilisateurDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactiverUtilisateur(Authentication authentication, @PathVariable Integer id) {
        utilisateurService.supprimerUtilisateur(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/statut")
    public ResponseEntity<UtilisateurDTO> changerStatut(
            Authentication authentication,
            @PathVariable Integer id,
            @Valid @RequestBody UtilisateurStatutDTO statutDTO) {
        return ResponseEntity.ok(utilisateurService.changerStatut(
                id, statutDTO.actif(), authentication.getName()));
    }

    @GetMapping("/recherche")
    public ResponseEntity<UtilisateurDTO> rechercherParEmail(@RequestParam String email) {
        return ResponseEntity.ok(utilisateurService.rechercherParEmail(email));
    }
}
