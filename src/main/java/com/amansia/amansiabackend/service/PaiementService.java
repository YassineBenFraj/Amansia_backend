package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.paiement.PaiementCreationDTO;
import com.amansia.amansiabackend.dto.paiement.PaiementDTO;
import com.amansia.amansiabackend.entity.Utilisateur;
import com.amansia.amansiabackend.dto.paiement.StripePreparationDTO;
import com.amansia.amansiabackend.dto.paiement.StripeFinalisationDTO;

import java.util.List;

public interface PaiementService {
    List<PaiementDTO> recupererTousLesPaiements();

    PaiementDTO creerPaiement(Utilisateur utilisateur, PaiementCreationDTO creationDTO);

    List<PaiementDTO> recupererPaiementsUtilisateur(Utilisateur utilisateur);

    PaiementDTO recupererPaiement(Utilisateur utilisateur, Integer paiementId);
    PaiementDTO verifierPaiementCarte(Utilisateur utilisateur, Integer paiementId, String paymentIntentId);

    void traiterWebhookStripe(String payload, String signature);
    StripePreparationDTO preparerPaiementCarte(Utilisateur utilisateur);
    PaiementDTO finaliserPaiementCarte(Utilisateur utilisateur, StripeFinalisationDTO dto);
}
