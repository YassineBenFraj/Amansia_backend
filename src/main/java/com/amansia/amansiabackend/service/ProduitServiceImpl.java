package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.produit.ProduitCreationDTO;
import com.amansia.amansiabackend.dto.produit.ProduitDTO;
import com.amansia.amansiabackend.dto.produit.ProduitUpdateDTO;
import com.amansia.amansiabackend.entity.Categorie;
import com.amansia.amansiabackend.entity.Produit;
import com.amansia.amansiabackend.exception.CategorieInactiveException;
import com.amansia.amansiabackend.exception.CategorieNotFoundException;
import com.amansia.amansiabackend.exception.PrixPromotionInvalideException;
import com.amansia.amansiabackend.exception.ProduitNotFoundException;
import com.amansia.amansiabackend.repository.CategorieRepository;
import com.amansia.amansiabackend.repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProduitServiceImpl implements ProduitService {

    private final ProduitRepository produitRepository;
    private final CategorieRepository categorieRepository;

    @Override
    public ProduitDTO creerProduit(ProduitCreationDTO creationDTO) {
        Categorie categorie = trouverCategorieActive(creationDTO.getCategorieId());
        verifierPrixPromotion(creationDTO.getPrix(), creationDTO.getPrixPromotion());

        Produit produit = new Produit();
        appliquerChamps(produit, creationDTO, categorie);
        produit.setActif(true);
        return convertirEnDTO(produitRepository.save(produit));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProduitDTO> recupererProduitsActifs() {
        return produitRepository.findByActifTrue().stream()
                .map(this::convertirEnDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProduitDTO> recupererTousLesProduits() {
        return produitRepository.findAll().stream()
                .map(this::convertirEnDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProduitDTO recupererProduitParId(Integer id) {
        return convertirEnDTO(trouverProduit(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProduitDTO> recupererProduitsParCategorie(Integer categorieId) {
        verifierCategorieExiste(categorieId);
        return produitRepository.findByCategorieId(categorieId).stream()
                .map(this::convertirEnDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProduitDTO> recupererProduitsActifsParCategorie(Integer categorieId) {
        verifierCategorieExiste(categorieId);
        return produitRepository.findByCategorieIdAndActifTrue(categorieId).stream()
                .map(this::convertirEnDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProduitDTO> rechercherParNom(String nom) {
        return produitRepository.findByNomContainingIgnoreCase(nom.trim()).stream()
                .filter(produit -> Boolean.TRUE.equals(produit.getActif()))
                .map(this::convertirEnDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProduitDTO> rechercherCatalogue(String nom, Integer categorieId, BigDecimal prixMin, BigDecimal prixMax) {
        String recherche = nom == null || nom.isBlank() ? null : nom.trim();
        if (prixMin != null && prixMin.signum() < 0 || prixMax != null && prixMax.signum() < 0) {
            throw new IllegalArgumentException("Les prix doivent être positifs ou nuls.");
        }
        if (prixMin != null && prixMax != null && prixMin.compareTo(prixMax) > 0) {
            throw new IllegalArgumentException("Le prix minimum ne peut pas dépasser le prix maximum.");
        }
        return produitRepository.rechercherCatalogue(recherche, categorieId, prixMin, prixMax).stream()
                .map(this::convertirEnDTO).toList();
    }

    @Override
    public ProduitDTO modifierProduit(Integer id, ProduitUpdateDTO updateDTO) {
        Produit produit = trouverProduit(id);
        Categorie categorie = trouverCategorieActive(updateDTO.getCategorieId());
        verifierPrixPromotion(updateDTO.getPrix(), updateDTO.getPrixPromotion() != null ? updateDTO.getPrixPromotion() : produit.getPrixPromotion());

        appliquerChamps(produit, updateDTO, categorie);
        if (updateDTO.getActif() != null) {
            produit.setActif(updateDTO.getActif());
        }
        return convertirEnDTO(produitRepository.save(produit));
    }

    @Override
    public void desactiverProduit(Integer id) {
        Produit produit = trouverProduit(id);
        produit.setActif(false);
        produitRepository.save(produit);
    }

    private Categorie trouverCategorieActive(Integer categorieId) {
        Categorie categorie = verifierCategorieExiste(categorieId);
        if (!Boolean.TRUE.equals(categorie.getActif())) {
            throw new CategorieInactiveException(categorieId);
        }
        return categorie;
    }

    private Categorie verifierCategorieExiste(Integer categorieId) {
        return categorieRepository.findById(categorieId)
                .orElseThrow(() -> new CategorieNotFoundException(categorieId));
    }

    private Produit trouverProduit(Integer id) {
        return produitRepository.findById(id)
                .orElseThrow(() -> new ProduitNotFoundException(id));
    }

    private void verifierPrixPromotion(BigDecimal prix, BigDecimal prixPromotion) {
        if (prixPromotion != null && (prixPromotion.signum() < 0 || prixPromotion.compareTo(prix) >= 0)) {
            throw new PrixPromotionInvalideException();
        }
    }

    private void appliquerChamps(Produit produit, ProduitCreationDTO dto, Categorie categorie) {
        produit.setCategorie(categorie);
        produit.setNom(dto.getNom().trim());
        produit.setDescription(dto.getDescription());
        produit.setPrix(dto.getPrix());
        if (dto.getPrixPromotion() != null) produit.setPrixPromotion(dto.getPrixPromotion());
        produit.setMarque(dto.getMarque());
        produit.setLargeur(dto.getLargeur());
        produit.setHauteur(dto.getHauteur());
        produit.setProfondeur(dto.getProfondeur());
        produit.setPoids(dto.getPoids());
        produit.setPersonnalisable(dto.getPersonnalisable());
    }

    private void appliquerChamps(Produit produit, ProduitUpdateDTO dto, Categorie categorie) {
        produit.setCategorie(categorie);
        produit.setNom(dto.getNom().trim());
        produit.setDescription(dto.getDescription());
        produit.setPrix(dto.getPrix());
        if (dto.getPrixPromotion() != null) produit.setPrixPromotion(dto.getPrixPromotion());
        produit.setMarque(dto.getMarque());
        produit.setLargeur(dto.getLargeur());
        produit.setHauteur(dto.getHauteur());
        produit.setProfondeur(dto.getProfondeur());
        produit.setPoids(dto.getPoids());
        if (dto.getPersonnalisable() != null) {
            produit.setPersonnalisable(dto.getPersonnalisable());
        }
    }

    private ProduitDTO convertirEnDTO(Produit produit) {
        ProduitDTO dto = new ProduitDTO();
        dto.setId(produit.getId());
        dto.setCategorieId(produit.getCategorie().getId());
        dto.setCategorieNom(produit.getCategorie().getNom());
        dto.setNom(produit.getNom());
        dto.setDescription(produit.getDescription());
        dto.setPrix(produit.getPrix());
        dto.setPrixPromotion(PrixProduit.promotionApplicable(produit) ? produit.getPrixPromotion() : null);
        dto.setMarque(produit.getMarque());
        dto.setLargeur(produit.getLargeur());
        dto.setHauteur(produit.getHauteur());
        dto.setProfondeur(produit.getProfondeur());
        dto.setPoids(produit.getPoids());
        dto.setPersonnalisable(produit.getPersonnalisable());
        dto.setActif(produit.getActif());
        dto.setDateCreation(produit.getDateCreation());
        dto.setDateModification(produit.getDateModification());
        return dto;
    }
}
