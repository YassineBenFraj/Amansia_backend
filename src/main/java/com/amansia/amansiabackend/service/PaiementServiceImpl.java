package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.paiement.PaiementCreationDTO;
import com.amansia.amansiabackend.dto.paiement.PaiementDTO;
import com.amansia.amansiabackend.dto.paiement.StripePreparationDTO;
import com.amansia.amansiabackend.dto.paiement.StripeFinalisationDTO;
import com.amansia.amansiabackend.dto.commande.CommandeCreationDTO;
import com.amansia.amansiabackend.entity.Commande;
import com.amansia.amansiabackend.entity.Paiement;
import com.amansia.amansiabackend.entity.StatutCommande;
import com.amansia.amansiabackend.entity.StatutPaiement;
import com.amansia.amansiabackend.entity.Utilisateur;
import com.amansia.amansiabackend.exception.CommandeAccessDeniedException;
import com.amansia.amansiabackend.exception.CommandeNotFoundException;
import com.amansia.amansiabackend.exception.PaiementAlreadyExistsException;
import com.amansia.amansiabackend.exception.PaiementNotFoundException;
import com.amansia.amansiabackend.repository.CommandeRepository;
import com.amansia.amansiabackend.repository.PaiementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class PaiementServiceImpl implements PaiementService {

    private final PaiementRepository paiementRepository;
    private final CommandeRepository commandeRepository;
    private final StripePaymentService stripePaymentService;
    private final PanierCommandeService panierCommandeService;
    private final PanierService panierService;
    private final CommandeService commandeService;
    private final StockService stockService;

    @Override
    @Transactional(readOnly = true)
    public List<PaiementDTO> recupererTousLesPaiements() {
        return paiementRepository.findAll(org.springframework.data.domain.Sort.by("dateCreation").descending()).stream()
            .map(this::convertirEnDTO).toList();
    }

    @Override
    public PaiementDTO creerPaiement(Utilisateur utilisateur, PaiementCreationDTO creationDTO) {
        if (utilisateur == null || utilisateur.getId() == null || creationDTO == null
                || creationDTO.getCommandeId() == null || creationDTO.getMethode() == null) {
            throw new IllegalArgumentException("L'utilisateur, la commande et la mÃ©thode sont obligatoires");
        }

        Commande commande = commandeRepository.findByIdForUpdate(creationDTO.getCommandeId())
                .orElseThrow(() -> new CommandeNotFoundException(creationDTO.getCommandeId()));
        if (commande.getUtilisateur() == null || !commande.getUtilisateur().getId().equals(utilisateur.getId())) {
            throw new CommandeAccessDeniedException();
        }
        if (commande.getStatut() == StatutCommande.ANNULEE) {
            throw new IllegalArgumentException("Une commande annulÃ©e ne peut pas Ãªtre payÃ©e");
        }
        BigDecimal montant = commande.getTotal();
        if (montant == null || montant.signum() <= 0) {
            throw new IllegalArgumentException("Le montant de la commande doit Ãªtre positif");
        }

        List<Paiement> paiementsExistants = paiementRepository.findByCommandeId(commande.getId());
        if (paiementsExistants.stream().anyMatch(paiement ->
            paiement.getStatut() == StatutPaiement.PAYE
                || paiement.getStatut() == StatutPaiement.EN_ATTENTE
                || paiement.getStatut() == StatutPaiement.REMBOURSE)) {
            throw new PaiementAlreadyExistsException(commande.getId());
        }

        Paiement paiement = new Paiement();
        paiement.setCommande(commande);
        paiement.setMontant(montant);
        paiement.setMethode(creationDTO.getMethode());
        paiement.setStatut(StatutPaiement.EN_ATTENTE);
        paiement.setReference(genererReference());
        paiement.setDatePaiement(null);

        String clientSecret = null;
        String publishableKey = null;
        if (creationDTO.getMethode() == com.amansia.amansiabackend.entity.MethodePaiement.CARTE) {
            publishableKey = stripePaymentService.getPublishableKey();
            clientSecret = stripePaymentService.creerPaymentIntent(paiement);
        }

        Paiement paiementEnregistre = paiementRepository.save(paiement);
        PaiementDTO dto = convertirEnDTO(paiementEnregistre);
        if (creationDTO.getMethode() == com.amansia.amansiabackend.entity.MethodePaiement.CARTE) {
            dto.setPublishableKey(publishableKey);
            dto.setClientSecret(clientSecret);
        } else {
            commande.setStatut(StatutCommande.CONFIRMEE);
            commandeRepository.save(commande);
            panierCommandeService.retirerArticlesCommandes(commande);
        }
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaiementDTO> recupererPaiementsUtilisateur(Utilisateur utilisateur) {
        if (utilisateur == null || utilisateur.getId() == null) {
            throw new IllegalArgumentException("L'utilisateur connectÃ© est obligatoire");
        }
        return paiementRepository.findByCommande_Utilisateur_IdOrderByDateCreationDesc(utilisateur.getId())
                .stream().map(this::convertirEnDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PaiementDTO recupererPaiement(Utilisateur utilisateur, Integer paiementId) {
        if (utilisateur == null || utilisateur.getId() == null) {
            throw new IllegalArgumentException("L'utilisateur connectÃ© est obligatoire");
        }
        Paiement paiement = paiementRepository.findByIdAndCommande_Utilisateur_Id(paiementId, utilisateur.getId())
                .orElseThrow(() -> new PaiementNotFoundException(paiementId));
        return convertirEnDTO(paiement);
    }

    @Override
    public PaiementDTO verifierPaiementCarte(Utilisateur utilisateur, Integer paiementId, String paymentIntentId) {
        Paiement paiement = paiementRepository.findByIdAndCommande_Utilisateur_Id(paiementId, utilisateur.getId())
                .orElseThrow(() -> new PaiementNotFoundException(paiementId));
        stripePaymentService.verifierPaymentIntent(paiement, paymentIntentId);
        return convertirEnDTO(paiement);
    }

    @Override
    public void traiterWebhookStripe(String payload, String signature) {
        stripePaymentService.traiterWebhook(payload, signature);
    }

    @Override
    public StripePreparationDTO preparerPaiementCarte(Utilisateur utilisateur) {
        var panier = panierService.obtenirPanier(utilisateur);
        if (panier.getLignes() == null || panier.getLignes().isEmpty() || panier.getTotalGeneral() == null || panier.getTotalGeneral().signum() <= 0) {
            throw new IllegalArgumentException("Le panier est vide");
        }
        for (var ligne : panier.getLignes()) stockService.verifier(ligne.getVariante() == null ? null : ligne.getVariante().getId(), ligne.getQuantite());
        String reference = genererReference();
        String secret = stripePaymentService.preparerAutorisation(panier.getTotalGeneral(), reference, utilisateur.getId());
        return new StripePreparationDTO(secret, stripePaymentService.getPublishableKey());
    }

    @Override
    public PaiementDTO finaliserPaiementCarte(Utilisateur utilisateur, StripeFinalisationDTO dto) {
        Paiement existant = paiementRepository.findByReferenceForUpdate(dto.getPaymentIntentId()).orElse(null);
        if (existant != null) {
            if (!existant.getCommande().getUtilisateur().getId().equals(utilisateur.getId())) throw new CommandeAccessDeniedException();
            return convertirEnDTO(existant);
        }
        var panier = panierService.obtenirPanier(utilisateur);
        stripePaymentService.verifierPreparation(dto.getPaymentIntentId(), utilisateur.getId(), panier.getTotalGeneral());
        CommandeCreationDTO creation = new CommandeCreationDTO();
        creation.setAdresseLivraisonId(dto.getAdresseLivraisonId()); creation.setCommentaire(dto.getCommentaire());
        var commandeDto = commandeService.creerCommande(utilisateur, creation);
        Commande commande = commandeRepository.findByIdForUpdate(commandeDto.getId()).orElseThrow();
        commande.setStatut(StatutCommande.CONFIRMEE); commandeRepository.save(commande);
        for (var ligne : commandeDto.getLignes()) stockService.vendre(ligne.getVarianteId(), ligne.getQuantite(), commande.getNumeroCommande());
        Paiement paiement = new Paiement(); paiement.setCommande(commande); paiement.setMontant(commande.getTotal());
        paiement.setMethode(com.amansia.amansiabackend.entity.MethodePaiement.CARTE); paiement.setStatut(StatutPaiement.EN_ATTENTE);
        paiement.setReference(dto.getPaymentIntentId()); paiement.setDatePaiement(null);
        paiementRepository.saveAndFlush(paiement);
        stripePaymentService.capturerPreparation(dto.getPaymentIntentId());
        paiement.setStatut(StatutPaiement.PAYE); paiement.setDatePaiement(LocalDateTime.now());
        paiementRepository.save(paiement); panierCommandeService.retirerArticlesCommandes(commande);
        return convertirEnDTO(paiement);
    }

    private String genererReference() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return "PAY-" + date + "-" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    private PaiementDTO convertirEnDTO(Paiement paiement) {
        PaiementDTO dto = new PaiementDTO();
        dto.setId(paiement.getId());
        dto.setCommandeId(paiement.getCommande() != null ? paiement.getCommande().getId() : null);
        if (paiement.getCommande() != null) {
            dto.setNumeroCommande(paiement.getCommande().getNumeroCommande());
            var client = paiement.getCommande().getUtilisateur();
            if (client != null) {
                dto.setClientId(client.getId()); dto.setClientNom((client.getPrenom() + " " + client.getNom()).trim());
                dto.setClientEmail(client.getEmail());
            }
        }
        dto.setMontant(paiement.getMontant());
        dto.setMethode(paiement.getMethode());
        dto.setStatut(paiement.getStatut());
        dto.setReference(paiement.getReference());
        dto.setDatePaiement(paiement.getDatePaiement());
        dto.setDateCreation(paiement.getDateCreation());
        return dto;
    }
}
