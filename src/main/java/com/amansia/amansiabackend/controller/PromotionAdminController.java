package com.amansia.amansiabackend.controller;

import com.amansia.amansiabackend.dto.PromotionDTO;
import com.amansia.amansiabackend.dto.PromotionRequest;
import com.amansia.amansiabackend.service.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/promotions")
@RequiredArgsConstructor
public class PromotionAdminController {
    private final PromotionService service;
    @GetMapping public List<PromotionDTO> list() { return service.list(); }
    @PostMapping public PromotionDTO create(@Valid @RequestBody PromotionRequest request) { return service.save(request); }
    @PutMapping("/{id}") public PromotionDTO update(@PathVariable Integer id, @Valid @RequestBody PromotionRequest request) {
        if (!id.equals(request.produitId())) throw new IllegalArgumentException("Le produit ne correspond pas.");
        return service.save(request);
    }
    @PatchMapping("/{id}/statut") public PromotionDTO status(@PathVariable Integer id, @RequestParam boolean actif) {
        return service.status(id, actif);
    }
}
