package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.panier.LignePanierCreationDTO;
import com.amansia.amansiabackend.dto.panier.LignePanierDTO;
import com.amansia.amansiabackend.dto.panier.LignePanierUpdateDTO;
import com.amansia.amansiabackend.dto.panier.PanierDTO;
import com.amansia.amansiabackend.dto.produit.ProduitDTO;
import com.amansia.amansiabackend.dto.variante.VarianteProduitDTO;
import com.amansia.amansiabackend.entity.LignePanier;
import com.amansia.amansiabackend.entity.Panier;
import com.amansia.amansiabackend.entity.Produit;
import com.amansia.amansiabackend.entity.Utilisateur;
import com.amansia.amansiabackend.entity.VarianteProduit;
import com.amansia.amansiabackend.exception.LignePanierNotFoundException;
import com.amansia.amansiabackend.exception.PanierAccessDeniedException;
import com.amansia.amansiabackend.exception.PanierNotFoundException;
import com.amansia.amansiabackend.exception.ProduitInactiveException;
import com.amansia.amansiabackend.exception.ProduitNotFoundException;
import com.amansia.amansiabackend.exception.QuantiteInvalideException;
import com.amansia.amansiabackend.exception.VarianteProduitInvalideException;
import com.amansia.amansiabackend.exception.VarianteProduitNotFoundException;
import com.amansia.amansiabackend.repository.LignePanierRepository;
import com.amansia.amansiabackend.repository.PanierRepository;
import com.amansia.amansiabackend.repository.ProduitRepository;
import com.amansia.amansiabackend.repository.VarianteProduitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class PanierServiceImpl implements PanierService {

    private final PanierRepository panierRepository;
    private final LignePanierRepository lignePanierRepository;
    private final ProduitRepository produitRepository;
    private final VarianteProduitRepository varianteProduitRepository;
    private final StockService stockService;

    @Override
    @Transactional
    public PanierDTO obtenirPanier(Utilisateur utilisateur) {
        Panier panier = trouverOuCreerPanier(utilisateur);
        return convertirEnDTO(panier);
    }

    @Override
    public PanierDTO ajouterLignePanier(Utilisateur utilisateur, LignePanierCreationDTO creationDTO) {
        if (creationDTO == null || creationDTO.getProduitId() == null) {
            throw new ProduitNotFoundException(null);
        }
        validerQuantite(creationDTO.getQuantite());

        Produit produit = produitRepository.findById(creationDTO.getProduitId())
                .orElseThrow(() -> new ProduitNotFoundException(creationDTO.getProduitId()));
        if (!Boolean.TRUE.equals(produit.getActif())) {
            throw new ProduitInactiveException(produit.getId());
        }

        VarianteProduit variante = null;
        if (creationDTO.getVarianteId() != null) {
            variante = varianteProduitRepository.findById(creationDTO.getVarianteId())
                    .orElseThrow(() -> new VarianteProduitNotFoundException(creationDTO.getVarianteId()));
            if (!Boolean.TRUE.equals(variante.getActif())) {
                throw new VarianteProduitInvalideException("La variante produit sélectionnée est inactive");
            }
            if (variante.getProduit() == null || !Objects.equals(variante.getProduit().getId(), produit.getId())) {
                throw new VarianteProduitInvalideException("La variante produit ne correspond pas au produit sélectionné");
            }
        }

        Panier panier = trouverOuCreerPanier(utilisateur);
        List<LignePanier> lignes = lignePanierRepository.findByPanierId(panier.getId());

        for (LignePanier ligne : lignes) {
            boolean memeProduit = Objects.equals(ligne.getProduit().getId(), produit.getId());
            boolean memeVariante = Objects.equals(
                    ligne.getVariante() != null ? ligne.getVariante().getId() : null,
                    variante != null ? variante.getId() : null
            );
            if (memeProduit && memeVariante) {
                stockService.verifier(variante == null ? null : variante.getId(), ligne.getQuantite() + creationDTO.getQuantite());
                ligne.setQuantite(ligne.getQuantite() + creationDTO.getQuantite());
                ligne.setPrixUnitaire(calculerPrixUnitaire(produit, variante));
                lignePanierRepository.save(ligne);
                return convertirEnDTO(panier);
            }
        }

        stockService.verifier(variante == null ? null : variante.getId(), creationDTO.getQuantite());
        LignePanier ligne = new LignePanier();
        ligne.setPanier(panier);
        ligne.setProduit(produit);
        ligne.setVariante(variante);
        ligne.setQuantite(creationDTO.getQuantite());
        ligne.setPrixUnitaire(calculerPrixUnitaire(produit, variante));
        lignePanierRepository.save(ligne);

        return convertirEnDTO(panier);
    }

    @Override
    public PanierDTO modifierLignePanier(Utilisateur utilisateur, Integer ligneId, LignePanierUpdateDTO updateDTO) {
        if (updateDTO == null || updateDTO.getQuantite() == null) {
            throw new QuantiteInvalideException();
        }
        validerQuantite(updateDTO.getQuantite());

        LignePanier ligne = lignePanierRepository.findById(ligneId)
                .orElseThrow(() -> new LignePanierNotFoundException(ligneId));
        if (!Objects.equals(ligne.getPanier().getUtilisateur().getId(), utilisateur.getId())) {
            throw new PanierAccessDeniedException();
        }

        stockService.verifier(ligne.getVariante() == null ? null : ligne.getVariante().getId(), updateDTO.getQuantite());
        ligne.setQuantite(updateDTO.getQuantite());
        lignePanierRepository.save(ligne);
        return convertirEnDTO(ligne.getPanier());
    }

    @Override
    public void supprimerLignePanier(Utilisateur utilisateur, Integer ligneId) {
        LignePanier ligne = lignePanierRepository.findById(ligneId)
                .orElseThrow(() -> new LignePanierNotFoundException(ligneId));
        if (!Objects.equals(ligne.getPanier().getUtilisateur().getId(), utilisateur.getId())) {
            throw new PanierAccessDeniedException();
        }
        lignePanierRepository.delete(ligne);
    }

    @Override
    public void viderPanier(Utilisateur utilisateur) {
        Panier panier = panierRepository.findByUtilisateurId(utilisateur.getId()).orElse(null);
        if (panier != null) {
            lignePanierRepository.deleteByPanierId(panier.getId());
        }
    }

    private Panier trouverOuCreerPanier(Utilisateur utilisateur) {
        return panierRepository.findByUtilisateurId(utilisateur.getId())
                .orElseGet(() -> {
                    Panier panier = new Panier();
                    panier.setUtilisateur(utilisateur);
                    return panierRepository.save(panier);
                });
    }

    private BigDecimal calculerPrixUnitaire(Produit produit, VarianteProduit variante) {
        return PrixProduit.calculer(produit, variante);
    }

    private void validerQuantite(Integer quantite) {
        if (quantite == null || quantite <= 0) {
            throw new QuantiteInvalideException();
        }
    }

    private PanierDTO convertirEnDTO(Panier panier) {
        PanierDTO dto = new PanierDTO();
        dto.setId(panier.getId());

        List<LignePanier> lignes = lignePanierRepository.findByPanierId(panier.getId());
        List<LignePanierDTO> ligneDTOs = lignes.stream()
                .map(this::convertirEnLigneDTO)
                .toList();

        dto.setLignes(ligneDTOs);
        dto.setTotalGeneral(ligneDTOs.stream()
                .map(LignePanierDTO::getPrixTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        BigDecimal before = lignes.stream().map(ligne -> {
            BigDecimal normal = ligne.getProduit().getPrix();
            if (ligne.getVariante() != null && ligne.getVariante().getPrixSupplementaire() != null)
                normal = normal.add(ligne.getVariante().getPrixSupplementaire());
            return normal.multiply(BigDecimal.valueOf(ligne.getQuantite()));
        }).reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setSousTotalAvantPromotion(before);
        dto.setRemiseProduits(before.subtract(dto.getTotalGeneral()));
        return dto;
    }

    private LignePanierDTO convertirEnLigneDTO(LignePanier ligne) {
        LignePanierDTO dto = new LignePanierDTO();
        dto.setId(ligne.getId());
        dto.setQuantite(ligne.getQuantite());
        BigDecimal prixActuel = PrixProduit.calculer(ligne.getProduit(), ligne.getVariante());
        dto.setPrixUnitaire(prixActuel);

        Produit produit = ligne.getProduit();
        ProduitDTO produitDTO = new ProduitDTO();
        produitDTO.setId(produit.getId());
        produitDTO.setNom(produit.getNom());
        produitDTO.setPrix(produit.getPrix());
        produitDTO.setActif(produit.getActif());
        dto.setProduit(produitDTO);

        if (ligne.getVariante() != null) {
            VarianteProduitDTO varianteDTO = new VarianteProduitDTO();
            varianteDTO.setId(ligne.getVariante().getId());
            varianteDTO.setProduitId(ligne.getVariante().getProduit() != null ? ligne.getVariante().getProduit().getId() : null);
            varianteDTO.setReference(ligne.getVariante().getReference());
            varianteDTO.setPrixSupplementaire(ligne.getVariante().getPrixSupplementaire());
            varianteDTO.setActif(ligne.getVariante().getActif());
            dto.setVariante(varianteDTO);
        }

        BigDecimal prixTotal = prixActuel.multiply(BigDecimal.valueOf(ligne.getQuantite()));
        dto.setPrixTotal(prixTotal);
        return dto;
    }
}
