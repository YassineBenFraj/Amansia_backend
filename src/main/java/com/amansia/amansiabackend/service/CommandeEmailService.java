package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.auth.service.EmailService;
import com.amansia.amansiabackend.entity.Commande;
import com.amansia.amansiabackend.entity.Paiement;
import com.amansia.amansiabackend.entity.StatutCommande;
import com.amansia.amansiabackend.entity.Utilisateur;
import com.amansia.amansiabackend.repository.AdresseRepository;
import com.amansia.amansiabackend.repository.LigneCommandeRepository;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.util.HtmlUtils;

@Service
@RequiredArgsConstructor
public class CommandeEmailService {
    private static final Logger log = LoggerFactory.getLogger(CommandeEmailService.class);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");
    private final EmailService email;
    private final LigneCommandeRepository lignes;
    private final AdresseRepository adresses;

    public void paiementConfirme(Commande commande, Paiement paiement) {
        var client = commande.getUtilisateur();
        String nom = nom(client);
        String numero = commande.getNumeroCommande();
        String montant = prix(paiement.getMontant());
        String date = date(paiement.getDatePaiement());
        String reference = paiement.getReference();
        List<String> articles = lignes.findByCommandeId(commande.getId()).stream()
                .map(l -> l.getQuantite() + " × " + (l.getProduit() == null ? "Article" : l.getProduit().getNom()))
                .toList();
        String adresse = adresses.findById(commande.getAdresseLivraisonId())
                .map(a -> String.join(", ", a.getAdresse(), a.getCodePostal() == null ? "" : a.getCodePostal(), a.getVille(), a.getPays()))
                .orElse("");
        String remise = commande.getRemise() != null && commande.getRemise().signum() > 0
                ? "Remise : " + prix(commande.getRemise()) + (commande.getCodePromo() == null ? "" : " (" + commande.getCodePromo() + ")") : "";
        String remiseProduits = commande.getRemiseProduits() != null && commande.getRemiseProduits().signum() > 0
                ? "Promotion produits : " + prix(commande.getRemiseProduits()) : "";
        String sujet = "Paiement confirmé - Commande #" + numero + " | Amansia";
        String texte = "Bonjour " + nom + ",\n\nVotre paiement a été confirmé et votre commande #" + numero
                + " est maintenant prise en charge.\nDate : " + date + "\nMontant : " + montant
                + "\nRéférence du paiement : " + reference + "\nStatut : Confirmée\n\nArticles :\n"
                + String.join("\n", articles) + "\n" + remise + "\n" + remiseProduits
                + (adresse.isBlank() ? "" : "\nAdresse de livraison : " + adresse) + "\n\nAmansia";
        StringBuilder details = new StringBuilder();
        for (String article : articles) details.append("<tr><td style='padding:7px 0;border-bottom:1px solid #e9e5dd'>").append(escape(article)).append("</td></tr>");
        if (!remise.isBlank()) details.append(row(remise));
        if (!remiseProduits.isBlank()) details.append(row(remiseProduits));
        if (!adresse.isBlank()) details.append(row("Livraison : " + adresse));
        String html = card("Paiement et commande confirmés", "Bonjour " + escape(nom) + ",",
                "Votre paiement a été confirmé. Votre commande est maintenant prise en charge.",
                row("Commande #" + numero) + row("Date : " + date) + row("Montant payé : " + montant)
                        + row("Référence du paiement : " + reference) + row("Statut : Confirmée")
                        + "<tr><td style='padding:14px 0 5px;font-weight:bold'>Vos articles</td></tr>" + details);
        apresCommit(client.getEmail(), sujet, texte, html, commande.getId());
    }

    public void commandeOfferte(Commande commande) {
        var client = commande.getUtilisateur();
        String numero = commande.getNumeroCommande();
        String nom = nom(client);
        List<String> articles = lignes.findByCommandeId(commande.getId()).stream()
                .map(l -> l.getQuantite() + " × " + (l.getProduit() == null ? "Article" : l.getProduit().getNom()))
                .toList();
        String adresse = adresses.findById(commande.getAdresseLivraisonId())
                .map(a -> String.join(", ", a.getAdresse(), a.getCodePostal() == null ? "" : a.getCodePostal(), a.getVille(), a.getPays()))
                .orElse("");
        String sujet = "Commande confirmée - #" + numero + " | Amansia";
        String texte = "Bonjour " + nom + ",\nVotre commande #" + numero + " a été confirmée. Aucun paiement n'est nécessaire.\nDate : "
                + date(commande.getDateCommande()) + "\nStatut : Confirmée\nTotal : " + prix(commande.getTotal())
                + "\nArticles :\n" + String.join("\n", articles)
                + (adresse.isBlank() ? "" : "\nAdresse de livraison : " + adresse) + "\n\nAmansia";
        StringBuilder details = new StringBuilder(row("Commande #" + numero) + row("Date : " + date(commande.getDateCommande()))
                + row("Statut : Confirmée") + row("Total : " + prix(commande.getTotal())));
        for (String article : articles) details.append(row(article));
        if (!adresse.isBlank()) details.append(row("Livraison : " + adresse));
        String html = card("Commande confirmée", "Bonjour " + escape(nom) + ",",
                "Votre commande est prise en charge. Aucun paiement n'est nécessaire.",
                details.toString());
        apresCommit(client.getEmail(), sujet, texte, html, commande.getId());
    }

    public void statutModifie(Commande commande) {
        StatutCommande statut = commande.getStatut();
        String action = switch (statut) {
            case EXPEDIEE -> "a été expédiée";
            case EN_LIVRAISON -> "est en livraison";
            case LIVREE -> "a été livrée";
            default -> null;
        };
        if (action == null) return;
        String numero = commande.getNumeroCommande();
        String nom = nom(commande.getUtilisateur());
        String date = date(LocalDateTime.now());
        String sujet = "Votre commande #" + numero + " " + action + " | Amansia";
        String statutLisible = switch (statut) {
            case EXPEDIEE -> "Expédiée";
            case EN_LIVRAISON -> "En livraison";
            case LIVREE -> "Livrée";
            default -> "";
        };
        String texte = "Bonjour " + nom + ",\nVotre commande #" + numero + " " + action
                + ".\nNouveau statut : " + statutLisible + "\nMise à jour : " + date + "\n\nAmansia";
        String html = card("Suivi de votre commande", "Bonjour " + escape(nom) + ",",
                "Votre commande " + escape(action) + ".",
                row("Commande #" + numero) + row("Nouveau statut : " + statutLisible) + row("Mise à jour : " + date));
        apresCommit(commande.getUtilisateur().getEmail(), sujet, texte, html, commande.getId());
    }

    private void apresCommit(String destinataire, String sujet, String texte, String html, Integer commandeId) {
        Runnable send = () -> {
            try { email.envoyerTransactionnel(destinataire, sujet, texte, html); }
            catch (Exception e) { log.warn("Email transactionnel non envoyé pour la commande {} : {}", commandeId, e.getClass().getSimpleName()); }
        };
        if (!TransactionSynchronizationManager.isActualTransactionActive())
            throw new IllegalStateException("L'email de commande doit être planifié dans une transaction");
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() { send.run(); }
        });
    }

    private static String card(String titre, String salutation, String message, String details) {
        return "<div style='background:#f6f4ef;padding:28px 12px;font-family:Arial,sans-serif;color:#263c30'>"
                + "<div style='max-width:600px;margin:auto;background:#fff;padding:28px;border:1px solid #e9e5dd'>"
                + "<div style='font-family:Georgia,serif;font-size:28px;color:#344e41'>Amansia</div>"
                + "<h1 style='font-family:Georgia,serif;font-size:24px;margin:28px 0 14px'>" + escape(titre) + "</h1>"
                + "<p>" + salutation + "</p><p>" + message + "</p>"
                + "<table role='presentation' style='border-collapse:collapse;width:100%;margin:24px 0'>" + details + "</table>"
                + "<p style='color:#647269;font-size:13px'>Merci de votre confiance.<br>Amansia</p></div></div>";
    }
    private static String row(String valeur) {
        return "<tr><td style='padding:8px 0;border-bottom:1px solid #e9e5dd'>" + escape(valeur) + "</td></tr>";
    }
    private static String escape(String value) { return HtmlUtils.htmlEscape(value == null ? "" : value); }
    private static String nom(Utilisateur user) { return ((user.getPrenom() == null ? "" : user.getPrenom()) + " " + (user.getNom() == null ? "" : user.getNom())).trim(); }
    private static String date(LocalDateTime value) { return value == null ? "" : DATE.format(value); }
    private static String prix(BigDecimal value) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.FRANCE);
        format.setMinimumFractionDigits(3); format.setMaximumFractionDigits(3);
        return format.format(value) + " TND";
    }
}
