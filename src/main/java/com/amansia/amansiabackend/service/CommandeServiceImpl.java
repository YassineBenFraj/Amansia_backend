package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.commande.CommandeCreationDTO;
import com.amansia.amansiabackend.dto.commande.CommandeDTO;
import com.amansia.amansiabackend.dto.commande.LigneCommandeDTO;
import com.amansia.amansiabackend.entity.Commande;
import com.amansia.amansiabackend.entity.LigneCommande;
import com.amansia.amansiabackend.entity.LignePanier;
import com.amansia.amansiabackend.entity.Panier;
import com.amansia.amansiabackend.entity.Produit;
import com.amansia.amansiabackend.entity.StatutCommande;
import com.amansia.amansiabackend.entity.Utilisateur;
import com.amansia.amansiabackend.entity.VarianteProduit;
import com.amansia.amansiabackend.exception.CommandeAccessDeniedException;
import com.amansia.amansiabackend.exception.CommandeAnnulationImpossibleException;
import com.amansia.amansiabackend.exception.CommandeNotFoundException;
import com.amansia.amansiabackend.exception.PanierNotFoundException;
import com.amansia.amansiabackend.exception.ProduitInactiveException;
import com.amansia.amansiabackend.exception.ProduitNotFoundException;
import com.amansia.amansiabackend.exception.QuantiteInvalideException;
import com.amansia.amansiabackend.exception.VarianteProduitInvalideException;
import com.amansia.amansiabackend.repository.CommandeRepository;
import com.amansia.amansiabackend.repository.LigneCommandeRepository;
import com.amansia.amansiabackend.repository.LignePanierRepository;
import com.amansia.amansiabackend.repository.PanierRepository;
import com.amansia.amansiabackend.repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CommandeServiceImpl implements CommandeService {

    private final CommandeRepository commandeRepository;
    private final LigneCommandeRepository ligneCommandeRepository;
    private final PanierRepository panierRepository;
    private final LignePanierRepository lignePanierRepository;
    private final ProduitRepository produitRepository;
    private final StockService stockService;
    private final AdresseService adresseService;
    private final com.amansia.amansiabackend.repository.PaiementRepository paiementRepository;
    private final com.amansia.amansiabackend.repository.AdresseRepository adresseRepository;
    private final NotificationService notifications;

    @Override
    @Transactional(readOnly = true)
    public List<CommandeDTO> recupererTousLesCommandes() {
        return commandeRepository.findAll(org.springframework.data.domain.Sort.by("dateCommande").descending()).stream()
            .map(c -> convertirEnDTO(c, ligneCommandeRepository.findByCommandeId(c.getId()))).toList();
    }

    @Override
    public CommandeDTO creerCommande(Utilisateur utilisateur, CommandeCreationDTO creationDTO) {
        if (utilisateur == null || utilisateur.getId() == null) {
            throw new IllegalArgumentException("L'utilisateur connectÃ© est obligatoire");
        }
        if (creationDTO == null || creationDTO.getAdresseLivraisonId() == null) {
            throw new IllegalArgumentException("L'adresse de livraison est obligatoire");
        }
        adresseService.verifierPropriete(creationDTO.getAdresseLivraisonId(), utilisateur.getId());

        Panier panier = panierRepository.findByUtilisateurId(utilisateur.getId())
                .orElseThrow(() -> new PanierNotFoundException(utilisateur.getId()));

        List<LignePanier> lignesPanier = lignePanierRepository.findByPanierId(panier.getId());
        if (lignesPanier == null || lignesPanier.isEmpty()) {
            throw new QuantiteInvalideException();
        }

        List<LigneCommande> lignesCommande = new ArrayList<>();
        BigDecimal sousTotal = BigDecimal.ZERO;

        for (LignePanier lignePanier : lignesPanier) {
            Produit produit = produitRepository.findById(lignePanier.getProduit().getId())
                    .orElseThrow(() -> new ProduitNotFoundException(lignePanier.getProduit().getId()));
            if (!Boolean.TRUE.equals(produit.getActif())) {
                throw new ProduitInactiveException(produit.getId());
            }

            VarianteProduit variante = lignePanier.getVariante();
            if (variante != null && !Boolean.TRUE.equals(variante.getActif())) {
                throw new VarianteProduitInvalideException("La variante produit sÃ©lectionnÃ©e est inactive");
            }
            if (variante != null && variante.getProduit() != null && !Objects.equals(variante.getProduit().getId(), produit.getId())) {
                throw new VarianteProduitInvalideException("La variante produit ne correspond pas au produit sÃ©lectionnÃ©");
            }

            BigDecimal prixUnitaire = calculerPrixUnitaire(produit, variante);
            if (prixUnitaire == null || prixUnitaire.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Le prix unitaire du produit est invalide");
            }
            if (lignePanier.getQuantite() == null || lignePanier.getQuantite() <= 0) {
                throw new QuantiteInvalideException();
            }

            stockService.verifier(variante == null ? null : variante.getId(), lignePanier.getQuantite());
            BigDecimal prixLigne = prixUnitaire.multiply(BigDecimal.valueOf(lignePanier.getQuantite()));
            sousTotal = sousTotal.add(prixLigne);

            LigneCommande ligneCommande = new LigneCommande();
            ligneCommande.setCommande(null);
            ligneCommande.setProduit(produit);
            ligneCommande.setVariante(variante);
            ligneCommande.setQuantite(lignePanier.getQuantite());
            ligneCommande.setPrixUnitaire(prixUnitaire.setScale(2, RoundingMode.HALF_UP));
            ligneCommande.setPrixTotal(prixLigne.setScale(2, RoundingMode.HALF_UP));
            lignesCommande.add(ligneCommande);
        }

        BigDecimal remise = BigDecimal.ZERO;
        BigDecimal total = sousTotal.subtract(remise).setScale(2, RoundingMode.HALF_UP);

        Commande commande = new Commande();
        commande.setUtilisateur(utilisateur);
        commande.setAdresseLivraisonId(creationDTO.getAdresseLivraisonId());
        commande.setNumeroCommande(genererNumeroCommande());
        commande.setStatut(StatutCommande.EN_ATTENTE);
        commande.setSousTotal(sousTotal.setScale(2, RoundingMode.HALF_UP));
        commande.setFraisLivraison(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        commande.setRemise(remise.setScale(2, RoundingMode.HALF_UP));
        commande.setTotal(total);
        commande.setCommentaire(creationDTO.getCommentaire());
        commandeRepository.save(commande);

        for (LigneCommande ligneCommande : lignesCommande) {
            ligneCommande.setCommande(commande);
        }
        ligneCommandeRepository.saveAll(lignesCommande);

        return convertirEnDTO(commande, lignesCommande);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommandeDTO> recupererCommandesUtilisateur(Utilisateur utilisateur) {
        if (utilisateur == null || utilisateur.getId() == null) {
            throw new IllegalArgumentException("L'utilisateur connectÃ© est obligatoire");
        }
        List<Commande> commandes = commandeRepository.findByUtilisateurIdOrderByDateCommandeDesc(utilisateur.getId());
        List<CommandeDTO> resultats = new ArrayList<>();
        for (Commande commande : commandes) {
            resultats.add(convertirEnDTO(commande, ligneCommandeRepository.findByCommandeId(commande.getId())));
        }
        return resultats;
    }

    @Override
    @Transactional(readOnly = true)
    public CommandeDTO recupererCommandeParId(Utilisateur utilisateur, Integer commandeId) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new CommandeNotFoundException(commandeId));
        if (!Objects.equals(commande.getUtilisateur().getId(), utilisateur.getId())) {
            throw new CommandeAccessDeniedException();
        }
        return convertirEnDTO(commande, ligneCommandeRepository.findByCommandeId(commande.getId()));
    }

    @Override
    public CommandeDTO annulerCommande(Utilisateur utilisateur, Integer commandeId) {
        Commande commande = commandeRepository.findByIdForUpdate(commandeId)
                .orElseThrow(() -> new CommandeNotFoundException(commandeId));
        if (!Objects.equals(commande.getUtilisateur().getId(), utilisateur.getId())) {
            throw new CommandeAccessDeniedException();
        }
        if (commande.getStatut() == StatutCommande.ANNULEE) {
            throw new CommandeAnnulationImpossibleException("Cette commande est dÃ©jÃ  annulÃ©e");
        }
        if (commande.getStatut() != StatutCommande.EN_ATTENTE && commande.getStatut() != StatutCommande.CONFIRMEE) {
            throw new CommandeAnnulationImpossibleException("Annulation impossible : preparation ou livraison deja engagee");
        }
        if (paiementRepository.findByCommandeId(commandeId).stream().anyMatch(p ->
                p.getStatut() == com.amansia.amansiabackend.entity.StatutPaiement.PAYE
                || p.getStatut() == com.amansia.amansiabackend.entity.StatutPaiement.REMBOURSE
                || (p.getMethode() == com.amansia.amansiabackend.entity.MethodePaiement.CARTE
                    && p.getStatut() == com.amansia.amansiabackend.entity.StatutPaiement.EN_ATTENTE))) {
            throw new CommandeAnnulationImpossibleException("Annulation impossible : paiement effectue ou paiement carte en cours ; remboursement non disponible");
        }
        commande.setStatut(StatutCommande.ANNULEE);
        commandeRepository.save(commande);
        notifications.orderCancelled(commande);
        return convertirEnDTO(commande, ligneCommandeRepository.findByCommandeId(commande.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public CommandeDTO recupererCommandeAdmin(Integer commandeId) {
        Commande commande = commandeRepository.findById(commandeId).orElseThrow(() -> new CommandeNotFoundException(commandeId));
        return convertirEnDTO(commande, ligneCommandeRepository.findByCommandeId(commandeId));
    }

    @Override
    public CommandeDTO changerStatutAdmin(Integer commandeId, StatutCommande statut) {
        Commande commande = commandeRepository.findByIdForUpdate(commandeId).orElseThrow(() -> new CommandeNotFoundException(commandeId));
        StatutCommande prochain = switch (commande.getStatut()) {
            case CONFIRMEE -> StatutCommande.EN_PREPARATION;
            case EN_PREPARATION -> StatutCommande.EXPEDIEE;
            case EXPEDIEE -> StatutCommande.EN_LIVRAISON;
            case EN_LIVRAISON -> StatutCommande.LIVREE;
            default -> null;
        };
        if (prochain == null || prochain != statut) {
            throw new IllegalArgumentException("Transition de statut non autorisée : " + commande.getStatut() + " vers " + statut);
        }
        commande.setStatut(statut);
        commandeRepository.save(commande);
        notifications.orderStatus(commande);
        return convertirEnDTO(commande, ligneCommandeRepository.findByCommandeId(commandeId));
    }

    private BigDecimal calculerPrixUnitaire(Produit produit, VarianteProduit variante) {
        return PrixProduit.calculer(produit, variante);
    }

    private String genererNumeroCommande() {
        return "CMD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private CommandeDTO convertirEnDTO(Commande commande, List<LigneCommande> lignesCommande) {
        CommandeDTO dto = new CommandeDTO();
        dto.setId(commande.getId());
        dto.setUtilisateurId(commande.getUtilisateur() != null ? commande.getUtilisateur().getId() : null);
        if (commande.getUtilisateur() != null) {
            dto.setClientNom((commande.getUtilisateur().getPrenom() + " " + commande.getUtilisateur().getNom()).trim());
            dto.setClientEmail(commande.getUtilisateur().getEmail()); dto.setClientTelephone(commande.getUtilisateur().getTelephone());
        }
        dto.setAdresseLivraisonId(commande.getAdresseLivraisonId());
        adresseRepository.findById(commande.getAdresseLivraisonId()).ifPresent(a -> {
            dto.setAdresseLivraison(a.getAdresse()); dto.setVilleLivraison(a.getVille());
            dto.setCodePostalLivraison(a.getCodePostal()); dto.setPaysLivraison(a.getPays());
        });
        dto.setNumeroCommande(commande.getNumeroCommande());
        dto.setDateCommande(commande.getDateCommande());
        dto.setStatut(commande.getStatut());
        dto.setSousTotal(commande.getSousTotal());
        dto.setRemise(commande.getRemise());
        dto.setTotal(commande.getTotal());
        dto.setCommentaire(commande.getCommentaire());

        List<LigneCommandeDTO> lignes = new ArrayList<>();
        if (lignesCommande != null) {
            for (LigneCommande ligne : lignesCommande) {
                LigneCommandeDTO ligneDTO = new LigneCommandeDTO();
                ligneDTO.setId(ligne.getId());
                ligneDTO.setProduitId(ligne.getProduit() != null ? ligne.getProduit().getId() : null);
                ligneDTO.setProduitNom(ligne.getProduit() != null ? ligne.getProduit().getNom() : null);
                ligneDTO.setVarianteId(ligne.getVariante() != null ? ligne.getVariante().getId() : null);
                ligneDTO.setQuantite(ligne.getQuantite());
                ligneDTO.setPrixUnitaire(ligne.getPrixUnitaire());
                ligneDTO.setPrixTotal(ligne.getPrixTotal());
                lignes.add(ligneDTO);
            }
        }
        dto.setLignes(lignes);
        return dto;
    }
}

