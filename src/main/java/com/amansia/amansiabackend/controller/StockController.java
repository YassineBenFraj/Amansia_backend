package com.amansia.amansiabackend.controller;

import com.amansia.amansiabackend.dto.stock.*;
import com.amansia.amansiabackend.repository.UtilisateurRepository;
import com.amansia.amansiabackend.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {
    private final StockService service;
    private final com.amansia.amansiabackend.service.CataloguePublicService catalogue;
    private final UtilisateurRepository utilisateurs;

    @GetMapping("/produit/{id}")
    public List<StockDTO> produit(@PathVariable Integer id) { catalogue.verifierProduit(id); return service.stocksProduitPublic(id); }

    @GetMapping("/admin")
    public List<StockDTO> admin() { return service.stocks(); }

    @GetMapping("/admin/mouvements")
    public List<MouvementStockDTO> mouvements() { return service.mouvements(); }

    @PostMapping("/admin/mouvements")
    public StockDTO mouvement(@Valid @RequestBody MouvementStockRequest request, Authentication auth) {
        Integer utilisateurId = utilisateurs.findByEmail(auth.getName()).orElseThrow().getId();
        return service.mouvementAdmin(request,utilisateurId);
    }
}
