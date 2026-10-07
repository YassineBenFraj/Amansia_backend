package com.amansia.amansiabackend.controller;

import com.amansia.amansiabackend.dto.paiement.PaiementDTO;
import com.amansia.amansiabackend.dto.paiement.StripePreparationDTO;
import com.amansia.amansiabackend.dto.paiement.StripeFinalisationDTO;
import com.amansia.amansiabackend.dto.commande.CommandeCreationDTO;
import com.amansia.amansiabackend.dto.commande.CommandeDTO;
import com.amansia.amansiabackend.entity.Utilisateur;
import com.amansia.amansiabackend.exception.UtilisateurNotFoundException;
import com.amansia.amansiabackend.repository.UtilisateurRepository;
import com.amansia.amansiabackend.service.PaiementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/paiements")
@RequiredArgsConstructor
public class PaiementController {

    private final PaiementService paiementService;
    private final UtilisateurRepository utilisateurRepository;

    @GetMapping("/admin")
    public ResponseEntity<List<PaiementDTO>> administration() {
        return ResponseEntity.ok(paiementService.recupererTousLesPaiements());
    }

    @PostMapping("/stripe/preparer")
    public ResponseEntity<StripePreparationDTO> preparer(Authentication authentication, @RequestBody(required = false) java.util.Map<String,String> body) {
        return ResponseEntity.ok(paiementService.preparerPaiementCarte(recupererUtilisateurConnecte(authentication), body == null ? null : body.get("codePromo")));
    }

    @PostMapping("/stripe/finaliser")
    public ResponseEntity<PaiementDTO> finaliser(Authentication authentication, @Valid @RequestBody StripeFinalisationDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paiementService.finaliserPaiementCarte(recupererUtilisateurConnecte(authentication), dto));
    }

    @PostMapping("/commande-offerte")
    public ResponseEntity<CommandeDTO> commandeOfferte(Authentication authentication, @Valid @RequestBody CommandeCreationDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paiementService.finaliserCommandeOfferte(recupererUtilisateurConnecte(authentication), dto));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> recevoirWebhookStripe(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        paiementService.traiterWebhookStripe(payload, signature);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/mes-paiements")
    public ResponseEntity<List<PaiementDTO>> recupererMesPaiements(Authentication authentication) {
        Utilisateur utilisateur = recupererUtilisateurConnecte(authentication);
        return ResponseEntity.ok(paiementService.recupererPaiementsUtilisateur(utilisateur));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaiementDTO> recupererPaiement(Authentication authentication, @PathVariable Integer id) {
        Utilisateur utilisateur = recupererUtilisateurConnecte(authentication);
        return ResponseEntity.ok(paiementService.recupererPaiement(utilisateur, id));
    }

    private Utilisateur recupererUtilisateurConnecte(Authentication authentication) {
        return utilisateurRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UtilisateurNotFoundException("email", authentication.getName()));
    }
}
