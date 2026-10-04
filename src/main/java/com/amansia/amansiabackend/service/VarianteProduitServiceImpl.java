package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.variante.VarianteProduitCreationDTO;
import com.amansia.amansiabackend.dto.variante.VarianteProduitDTO;
import com.amansia.amansiabackend.dto.variante.VarianteProduitUpdateDTO;
import com.amansia.amansiabackend.entity.Couleur;
import com.amansia.amansiabackend.entity.Materiau;
import com.amansia.amansiabackend.entity.Produit;
import com.amansia.amansiabackend.entity.VarianteProduit;
import com.amansia.amansiabackend.exception.CouleurInactiveException;
import com.amansia.amansiabackend.exception.CouleurNotFoundException;
import com.amansia.amansiabackend.exception.MateriauInactiveException;
import com.amansia.amansiabackend.exception.MateriauNotFoundException;
import com.amansia.amansiabackend.exception.ProduitInactiveException;
import com.amansia.amansiabackend.exception.ProduitNotFoundException;
import com.amansia.amansiabackend.exception.ReferenceVarianteAlreadyUsedException;
import com.amansia.amansiabackend.exception.VarianteProduitNotFoundException;
import com.amansia.amansiabackend.repository.CouleurRepository;
import com.amansia.amansiabackend.repository.MateriauRepository;
import com.amansia.amansiabackend.repository.ProduitRepository;
import com.amansia.amansiabackend.repository.VarianteProduitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VarianteProduitServiceImpl implements VarianteProduitService {

    private final VarianteProduitRepository varianteProduitRepository;
    private final ProduitRepository produitRepository;
    private final MateriauRepository materiauRepository;
    private final CouleurRepository couleurRepository;

    @Override
    public VarianteProduitDTO creerVarianteProduit(VarianteProduitCreationDTO creationDTO) {
        Produit produit = trouverProduitValide(creationDTO.getProduitId());
        Materiau materiau = validerMateriauOptionnel(creationDTO.getMateriauId());
        Couleur couleur = validerCouleurOptionnelle(creationDTO.getCouleurId());
        String reference = normaliserReference(creationDTO.getReference());
        verifierReferenceDisponible(reference);
        verifierPrixSupplementaire(creationDTO.getPrixSupplementaire());

        VarianteProduit varianteProduit = new VarianteProduit();
        varianteProduit.setProduit(produit);
        varianteProduit.setMateriau(materiau);
        varianteProduit.setCouleur(couleur);
        varianteProduit.setReference(reference);
        varianteProduit.setPrixSupplementaire(creationDTO.getPrixSupplementaire());
        varianteProduit.setActif(creationDTO.getActif() != null ? creationDTO.getActif() : true);
        return convertirEnDTO(varianteProduitRepository.save(varianteProduit));
    }

    @Override
    @Transactional(readOnly = true)
    public VarianteProduitDTO recupererVarianteProduitParId(Integer id) {
        return convertirEnDTO(trouverVarianteProduit(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VarianteProduitDTO> recupererToutesLesVariantesProduit() {
        return varianteProduitRepository.findAll().stream()
                .map(this::convertirEnDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VarianteProduitDTO> recupererVariantesProduitParProduit(Integer produitId) {
        verifierProduitExiste(produitId);
        return varianteProduitRepository.findByProduitId(produitId).stream()
                .map(this::convertirEnDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VarianteProduitDTO> recupererVariantesProduitActivesParProduit(Integer produitId) {
        verifierProduitExiste(produitId);
        return varianteProduitRepository.findByProduitIdAndActifTrue(produitId).stream()
                .map(this::convertirEnDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VarianteProduitDTO> rechercherParReference(String reference) {
        String referenceRecherche = normaliserReference(reference);
        return varianteProduitRepository.findByReferenceContainingIgnoreCase(referenceRecherche).stream()
                .map(this::convertirEnDTO)
                .toList();
    }

    @Override
    public VarianteProduitDTO modifierVarianteProduit(Integer id, VarianteProduitUpdateDTO updateDTO) {
        VarianteProduit varianteProduit = trouverVarianteProduit(id);
        if (updateDTO.getMateriauId() != null) {
            varianteProduit.setMateriau(validerMateriauOptionnel(updateDTO.getMateriauId()));
        }
        if (updateDTO.getCouleurId() != null) {
            varianteProduit.setCouleur(validerCouleurOptionnelle(updateDTO.getCouleurId()));
        }
        if (updateDTO.getReference() != null) {
            String reference = normaliserReference(updateDTO.getReference());
            varianteProduitRepository.findByReferenceIgnoreCase(reference).ifPresent(varianteExistante -> {
                if (!varianteExistante.getId().equals(id)) {
                    throw new ReferenceVarianteAlreadyUsedException();
                }
            });
            varianteProduit.setReference(reference);
        }
        if (updateDTO.getPrixSupplementaire() != null) {
            verifierPrixSupplementaire(updateDTO.getPrixSupplementaire());
            varianteProduit.setPrixSupplementaire(updateDTO.getPrixSupplementaire());
        }
        if (updateDTO.getActif() != null) {
            varianteProduit.setActif(updateDTO.getActif());
        }
        return convertirEnDTO(varianteProduitRepository.save(varianteProduit));
    }

    @Override
    public void desactiverVarianteProduit(Integer id) {
        VarianteProduit varianteProduit = trouverVarianteProduit(id);
        varianteProduit.setActif(false);
        varianteProduitRepository.save(varianteProduit);
    }

    private Produit trouverProduitValide(Integer produitId) {
        Produit produit = verifierProduitExiste(produitId);
        if (!Boolean.TRUE.equals(produit.getActif())) {
            throw new ProduitInactiveException(produitId);
        }
        return produit;
    }

    private Produit verifierProduitExiste(Integer produitId) {
        return produitRepository.findById(produitId)
                .orElseThrow(() -> new ProduitNotFoundException(produitId));
    }

    private Materiau validerMateriauOptionnel(Integer materiauId) {
        if (materiauId == null) {
            return null;
        }
        Materiau materiau = materiauRepository.findById(materiauId)
                .orElseThrow(() -> new MateriauNotFoundException(materiauId));
        if (!Boolean.TRUE.equals(materiau.getActif())) {
            throw new MateriauInactiveException(materiauId);
        }
        return materiau;
    }

    private Couleur validerCouleurOptionnelle(Integer couleurId) {
        if (couleurId == null) {
            return null;
        }
        Couleur couleur = couleurRepository.findById(couleurId)
                .orElseThrow(() -> new CouleurNotFoundException(couleurId));
        if (!Boolean.TRUE.equals(couleur.getActif())) {
            throw new CouleurInactiveException(couleurId);
        }
        return couleur;
    }

    private VarianteProduit trouverVarianteProduit(Integer id) {
        return varianteProduitRepository.findById(id)
                .orElseThrow(() -> new VarianteProduitNotFoundException(id));
    }

    private void verifierReferenceDisponible(String reference) {
        if (varianteProduitRepository.existsByReferenceIgnoreCase(reference)) {
            throw new ReferenceVarianteAlreadyUsedException();
        }
    }

    private void verifierPrixSupplementaire(BigDecimal prixSupplementaire) {
        if (prixSupplementaire != null && prixSupplementaire.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le prix supplementaire doit etre superieur ou egal a 0");
        }
    }

    private String normaliserReference(String reference) {
        if (reference == null) {
            return "";
        }
        return reference.trim();
    }

    private VarianteProduitDTO convertirEnDTO(VarianteProduit varianteProduit) {
        VarianteProduitDTO dto = new VarianteProduitDTO();
        dto.setId(varianteProduit.getId());
        dto.setProduitId(varianteProduit.getProduit() != null ? varianteProduit.getProduit().getId() : null);
        dto.setMateriauId(varianteProduit.getMateriau() != null ? varianteProduit.getMateriau().getId() : null);
        dto.setCouleurId(varianteProduit.getCouleur() != null ? varianteProduit.getCouleur().getId() : null);
        dto.setReference(varianteProduit.getReference());
        dto.setPrixSupplementaire(varianteProduit.getPrixSupplementaire());
        dto.setActif(varianteProduit.getActif());
        dto.setDateCreation(varianteProduit.getDateCreation());
        return dto;
    }
}
