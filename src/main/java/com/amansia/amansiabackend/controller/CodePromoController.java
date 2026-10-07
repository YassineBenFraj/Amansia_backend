package com.amansia.amansiabackend.controller;

import com.amansia.amansiabackend.dto.*;
import com.amansia.amansiabackend.entity.Utilisateur;
import com.amansia.amansiabackend.repository.UtilisateurRepository;
import com.amansia.amansiabackend.service.CodePromoService;
import com.amansia.amansiabackend.service.PanierService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CodePromoController {
    private final CodePromoService codes;
    private final PanierService paniers;
    private final UtilisateurRepository users;

    @GetMapping("/api/admin/codes-promo")
    public List<CodePromoDTO> list() { return codes.list(); }
    @PostMapping("/api/admin/codes-promo")
    public CodePromoDTO create(@RequestBody CodePromoRequest request) { return codes.save(null, request); }
    @PutMapping("/api/admin/codes-promo/{id}")
    public CodePromoDTO update(@PathVariable Integer id, @RequestBody CodePromoRequest request) { return codes.save(id, request); }
    @PatchMapping("/api/admin/codes-promo/{id}/statut")
    public CodePromoDTO status(@PathVariable Integer id, @RequestParam boolean actif) { return codes.status(id, actif); }

    @PostMapping("/api/codes-promo/valider")
    public CodePromoQuote validate(Authentication authentication, @RequestBody java.util.Map<String,String> body) {
        Utilisateur user = users.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));
        return codes.quote(body.get("code"), user.getId(), paniers.obtenirPanier(user).getTotalGeneral());
    }
}
