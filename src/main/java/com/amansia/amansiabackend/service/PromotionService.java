package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.PromotionDTO;
import com.amansia.amansiabackend.dto.PromotionRequest;
import com.amansia.amansiabackend.entity.Produit;
import com.amansia.amansiabackend.repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionService {
    private final ProduitRepository produits;

    @Transactional(readOnly = true)
    public List<PromotionDTO> list() {
        return produits.findAll().stream().filter(p -> p.getPrixPromotion() != null).map(this::dto).toList();
    }

    @Transactional
    public PromotionDTO save(PromotionRequest request) {
        Produit produit = produits.findById(request.produitId())
                .orElseThrow(() -> new IllegalArgumentException("Produit introuvable."));
        if (request.prixPromotion().signum() < 0 || request.prixPromotion().compareTo(produit.getPrix()) >= 0)
            throw new IllegalArgumentException("Le prix promotionnel doit être inférieur au prix normal et positif.");
        if (!request.dateFin().isAfter(request.dateDebut()))
            throw new IllegalArgumentException("La date de fin doit suivre la date de début.");
        produit.setPrixPromotion(request.prixPromotion());
        produit.setPromotionDateDebut(request.dateDebut());
        produit.setPromotionDateFin(request.dateFin());
        produit.setPromotionActive(request.actif());
        return dto(produits.save(produit));
    }

    @Transactional
    public PromotionDTO status(Integer produitId, boolean active) {
        Produit produit = produits.findById(produitId)
                .orElseThrow(() -> new IllegalArgumentException("Produit introuvable."));
        if (produit.getPrixPromotion() == null) throw new IllegalArgumentException("Ce produit n'a pas de promotion.");
        produit.setPromotionActive(active);
        return dto(produits.save(produit));
    }

    private PromotionDTO dto(Produit p) {
        return new PromotionDTO(p.getId(), p.getNom(), p.getPrix(), p.getPrixPromotion(),
                p.getPromotionDateDebut(), p.getPromotionDateFin(), Boolean.TRUE.equals(p.getPromotionActive()),
                PrixProduit.promotionApplicable(p));
    }
}
