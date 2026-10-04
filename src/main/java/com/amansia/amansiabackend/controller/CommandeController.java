package com.amansia.amansiabackend.controller;

import com.amansia.amansiabackend.dto.commande.CommandeCreationDTO;
import com.amansia.amansiabackend.dto.commande.CommandeDTO;
import com.amansia.amansiabackend.dto.commande.StatutCommandeUpdateDTO;
import com.amansia.amansiabackend.entity.Utilisateur;
import com.amansia.amansiabackend.exception.UtilisateurNotFoundException;
import com.amansia.amansiabackend.repository.UtilisateurRepository;
import com.amansia.amansiabackend.service.CommandeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/commandes")
@RequiredArgsConstructor
public class CommandeController {

    private final CommandeService commandeService;
    private final UtilisateurRepository utilisateurRepository;

    @GetMapping("/admin")
    public ResponseEntity<List<CommandeDTO>> administration() {
        return ResponseEntity.ok(commandeService.recupererTousLesCommandes());
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<CommandeDTO> detailAdministration(@PathVariable Integer id) {
        return ResponseEntity.ok(commandeService.recupererCommandeAdmin(id));
    }

    @PutMapping("/admin/{id}/statut")
    public ResponseEntity<CommandeDTO> changerStatut(@PathVariable Integer id, @Valid @RequestBody StatutCommandeUpdateDTO dto) {
        return ResponseEntity.ok(commandeService.changerStatutAdmin(id, dto.statut()));
    }

    @PostMapping
    public ResponseEntity<CommandeDTO> creerCommande(
            Authentication authentication,
            @Valid @RequestBody CommandeCreationDTO creationDTO) {
        Utilisateur utilisateur = recupererUtilisateurConnecte(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(commandeService.creerCommande(utilisateur, creationDTO));
    }

    @GetMapping
    public ResponseEntity<List<CommandeDTO>> recupererCommandes(Authentication authentication) {
        Utilisateur utilisateur = recupererUtilisateurConnecte(authentication);
        return ResponseEntity.ok(commandeService.recupererCommandesUtilisateur(utilisateur));
    }

    @GetMapping("/mes-commandes")
    public ResponseEntity<List<CommandeDTO>> recupererMesCommandes(Authentication authentication) {
        Utilisateur utilisateur = recupererUtilisateurConnecte(authentication);
        return ResponseEntity.ok(commandeService.recupererCommandesUtilisateur(utilisateur));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommandeDTO> recupererCommandeParId(Authentication authentication, @PathVariable Integer id) {
        Utilisateur utilisateur = recupererUtilisateurConnecte(authentication);
        return ResponseEntity.ok(commandeService.recupererCommandeParId(utilisateur, id));
    }

    @PutMapping("/{id}/annuler")
    public ResponseEntity<CommandeDTO> annulerCommande(Authentication authentication, @PathVariable Integer id) {
        Utilisateur utilisateur = recupererUtilisateurConnecte(authentication);
        return ResponseEntity.ok(commandeService.annulerCommande(utilisateur, id));
    }

    private Utilisateur recupererUtilisateurConnecte(Authentication authentication) {
        return utilisateurRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UtilisateurNotFoundException("email", authentication.getName()));
    }
}
