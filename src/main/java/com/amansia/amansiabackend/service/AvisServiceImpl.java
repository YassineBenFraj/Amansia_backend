package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.avis.AvisCreationDTO;
import com.amansia.amansiabackend.dto.avis.AvisDTO;
import com.amansia.amansiabackend.dto.avis.AvisUpdateDTO;
import com.amansia.amansiabackend.entity.Avis;
import com.amansia.amansiabackend.entity.Commande;
import com.amansia.amansiabackend.entity.Produit;
import com.amansia.amansiabackend.entity.Utilisateur;
import com.amansia.amansiabackend.exception.AvisAccessDeniedException;
import com.amansia.amansiabackend.exception.AvisAlreadyExistsException;
import com.amansia.amansiabackend.exception.AvisNotFoundException;
import com.amansia.amansiabackend.exception.CommandeNotFoundException;
import com.amansia.amansiabackend.exception.ProduitNotFoundException;
import com.amansia.amansiabackend.repository.AvisRepository;
import com.amansia.amansiabackend.repository.CommandeRepository;
import com.amansia.amansiabackend.repository.LigneCommandeRepository;
import com.amansia.amansiabackend.repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class AvisServiceImpl implements AvisService {

    private final AvisRepository avisRepository;
    private final ProduitRepository produitRepository;
    private final CommandeRepository commandeRepository;
    private final LigneCommandeRepository ligneCommandeRepository;
    private final NotificationService notifications;

    @Override
    public AvisDTO creerAvis(Utilisateur utilisateur, AvisCreationDTO creationDTO) {
        if (utilisateur == null || utilisateur.getId() == null || creationDTO == null) {
            throw new IllegalArgumentException("L'utilisateur et les données de l'avis sont obligatoires");
        }
        validerNote(creationDTO.getNote());

        Produit produit = produitRepository.findById(creationDTO.getProduitId())
                .orElseThrow(() -> new ProduitNotFoundException(creationDTO.getProduitId()));
        Commande commande = commandeRepository.findByIdForUpdate(creationDTO.getCommandeId())
                .orElseThrow(() -> new CommandeNotFoundException(creationDTO.getCommandeId()));
        if (commande.getUtilisateur() == null || !Objects.equals(commande.getUtilisateur().getId(), utilisateur.getId())) {
            throw new AvisAccessDeniedException();
        }
        if (!ligneCommandeRepository.existsByCommandeIdAndProduitId(commande.getId(), produit.getId())) {
            throw new IllegalArgumentException("Ce produit ne figure pas dans les lignes de cette commande");
        }
        if (avisRepository.existsByUtilisateurIdAndProduitIdAndCommandeId(utilisateur.getId(), produit.getId(), commande.getId())) {
            throw new AvisAlreadyExistsException(produit.getId(), commande.getId());
        }

        Avis avis = new Avis();
        avis.setUtilisateur(utilisateur);
        avis.setProduit(produit);
        avis.setCommande(commande);
        avis.setNote(creationDTO.getNote().byteValue());
        avis.setCommentaire(normaliserCommentaire(creationDTO.getCommentaire()));
        avis.setApprouve(false);
        Avis saved = avisRepository.save(avis);
        notifications.newReview(saved.getId(), produit.getNom());
        return convertirEnDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvisDTO> recupererAvisProduit(Integer produitId, boolean administrateur, Integer utilisateurId) {
        if (!produitRepository.existsById(produitId)) {
            throw new ProduitNotFoundException(produitId);
        }
        List<Avis> avis = administrateur
                ? avisRepository.findByProduitIdOrderByDateCreationDesc(produitId)
                : utilisateurId != null
                    ? avisRepository.findPublicAndOwnedByProduct(produitId, utilisateurId)
                    : avisRepository.findByProduitIdAndApprouveTrueOrderByDateCreationDesc(produitId);
        return avis.stream().map(this::convertirEnDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvisDTO> recupererTousAvis() {
        return avisRepository.findAllByOrderByDateCreationDesc().stream()
                .map(this::convertirEnDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AvisDTO recupererAvis(Integer avisId, Utilisateur utilisateur, boolean administrateur) {
        Avis avis = avisRepository.findById(avisId).orElseThrow(() -> new AvisNotFoundException(avisId));
        if (!administrateur && !Boolean.TRUE.equals(avis.getApprouve())
                && (utilisateur == null || !Objects.equals(avis.getUtilisateur().getId(), utilisateur.getId()))) {
            throw new AvisNotFoundException(avisId);
        }
        return convertirEnDTO(avis);
    }

    @Override
    public AvisDTO modifierAvis(Integer avisId, Utilisateur utilisateur, AvisUpdateDTO updateDTO, boolean administrateur) {
        validerNote(updateDTO != null ? updateDTO.getNote() : null);
        Avis avis = avisRepository.findById(avisId).orElseThrow(() -> new AvisNotFoundException(avisId));
        verifierProprietaireOuAdministrateur(avis, utilisateur, administrateur);

        String commentaire = normaliserCommentaire(updateDTO.getCommentaire());
        if (Objects.equals(avis.getNote(), updateDTO.getNote().byteValue())
                && Objects.equals(avis.getCommentaire(), commentaire)) {
            return convertirEnDTO(avis);
        }
        avis.setNote(updateDTO.getNote().byteValue());
        avis.setCommentaire(commentaire);
        if (!administrateur) {
            avis.setApprouve(false);
        }
        Avis saved = avisRepository.save(avis);
        if (!administrateur) {
            notifications.reviewUpdated(saved.getId(), saved.getProduit().getNom());
        }
        return convertirEnDTO(saved);
    }

    @Override
    public AvisDTO modererAvis(Integer avisId, boolean approuve) {
        Avis avis = avisRepository.findById(avisId).orElseThrow(() -> new AvisNotFoundException(avisId));
        avis.setApprouve(approuve);
        return convertirEnDTO(avisRepository.save(avis));
    }

    @Override
    public void supprimerAvis(Integer avisId, Utilisateur utilisateur, boolean administrateur) {
        Avis avis = avisRepository.findById(avisId).orElseThrow(() -> new AvisNotFoundException(avisId));
        verifierProprietaireOuAdministrateur(avis, utilisateur, administrateur);
        avisRepository.delete(avis);
    }

    private void verifierProprietaireOuAdministrateur(Avis avis, Utilisateur utilisateur, boolean administrateur) {
        if (!administrateur && (utilisateur == null
                || !Objects.equals(avis.getUtilisateur().getId(), utilisateur.getId()))) {
            throw new AvisAccessDeniedException();
        }
    }

    private void validerNote(Integer note) {
        if (note == null || note < 1 || note > 5) {
            throw new IllegalArgumentException("La note doit être comprise entre 1 et 5");
        }
    }

    private String normaliserCommentaire(String commentaire) {
        if (commentaire == null || commentaire.isBlank()) {
            return null;
        }
        return commentaire.trim();
    }

    private AvisDTO convertirEnDTO(Avis avis) {
        AvisDTO dto = new AvisDTO();
        dto.setId(avis.getId());
        dto.setUtilisateurId(avis.getUtilisateur() != null ? avis.getUtilisateur().getId() : null);
        dto.setUtilisateurNom(avis.getUtilisateur() == null ? null : (avis.getUtilisateur().getPrenom() + " " + avis.getUtilisateur().getNom()).trim());
        dto.setProduitId(avis.getProduit() != null ? avis.getProduit().getId() : null);
        dto.setProduitNom(avis.getProduit() != null ? avis.getProduit().getNom() : null);
        dto.setCommandeId(avis.getCommande() != null ? avis.getCommande().getId() : null);
        dto.setNote(avis.getNote() != null ? avis.getNote().intValue() : null);
        dto.setCommentaire(avis.getCommentaire());
        dto.setApprouve(avis.getApprouve());
        dto.setDateCreation(avis.getDateCreation());
        return dto;
    }
}
