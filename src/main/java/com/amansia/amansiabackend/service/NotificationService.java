package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.NotificationDTO;
import com.amansia.amansiabackend.entity.*;
import com.amansia.amansiabackend.repository.*;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {
    private final NotificationRepository notifications;
    private final UtilisateurRepository users;
    private final FavoriRepository favoris;
    private final JdbcTemplate jdbc;

    @Transactional(readOnly = true)
    public List<NotificationDTO> list(String email) {
        return notifications.findByUtilisateurIdOrderByDateCreationDescIdDesc(userId(email)).stream().map(this::dto).toList();
    }
    @Transactional(readOnly = true)
    public long count(String email) { return notifications.countByUtilisateurIdAndEstLueFalse(userId(email)); }
    public NotificationDTO read(String email, Integer id) {
        Notification item = notifications.findByIdAndUtilisateurId(id, userId(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification introuvable"));
        if (!item.getEstLue()) { item.setEstLue(true); notifications.save(item); }
        return dto(item);
    }
    public void readAll(String email) { notifications.markAllRead(userId(email)); }

    public void orderPaid(Commande order) {
        Integer id = order.getId();
        String number = order.getNumeroCommande();
        Integer owner = order.getUtilisateur().getId();
        String link = "/mes-commandes/" + id;
        create(owner, TypeNotification.PAIEMENT_CONFIRME, "Paiement confirmé", "Votre paiement pour la commande #" + number + " a été confirmé.", link, "payment:" + id);
        create(owner, TypeNotification.COMMANDE_CONFIRMEE, "Commande confirmée", "Votre commande #" + number + " a été confirmée.", link, "confirmed:" + id);
        admins(TypeNotification.NOUVELLE_COMMANDE, "Nouvelle commande", "Une nouvelle commande payée #" + number + " vient d'être enregistrée.", "/admin/commandes/" + id, "new-order:" + id);
    }

    public void orderStatus(Commande order) {
        String detail = switch (order.getStatut()) {
            case EN_PREPARATION -> "est maintenant en préparation";
            case EXPEDIEE -> "a été expédiée";
            case EN_LIVRAISON -> "est en cours de livraison";
            case LIVREE -> "a été livrée";
            default -> null;
        };
        String title = switch (order.getStatut()) {
            case EN_PREPARATION -> "Commande en préparation";
            case EXPEDIEE -> "Commande expédiée";
            case EN_LIVRAISON -> "Commande en livraison";
            case LIVREE -> "Commande livrée";
            default -> null;
        };
        if (detail != null) create(order.getUtilisateur().getId(), TypeNotification.COMMANDE_STATUT,
                title,
                "Votre commande #" + order.getNumeroCommande() + " " + detail + ".",
                "/mes-commandes/" + order.getId(), "status:" + order.getId() + ":" + order.getStatut());
    }

    public void orderCancelled(Commande order) {
        create(order.getUtilisateur().getId(), TypeNotification.COMMANDE_ANNULEE, "Commande annulée",
                "Votre commande #" + order.getNumeroCommande() + " a été annulée.",
                "/mes-commandes/" + order.getId(), "cancelled:" + order.getId());
    }

    public void stockChanged(Integer productId, String productName, Integer variantId, String reference,
                             int before, int after, int threshold, boolean productWasAvailable, boolean productAvailable, String eventKey) {
        if (before > threshold && after > 0 && after <= threshold) admins(TypeNotification.STOCK_FAIBLE,
                "Stock faible", "Le stock de la variante " + reference + " du produit « " + productName + " » est faible.",
                "/admin/stocks", "low:" + eventKey);
        if (before > 0 && after == 0) admins(TypeNotification.RUPTURE_STOCK,
                "Rupture de stock", "La variante " + reference + " du produit « " + productName + " » est en rupture.",
                "/admin/stocks", "out:" + eventKey);
        if (!productWasAvailable && productAvailable) {
            for (Integer owner : favoris.activeUserIdsForProduct(productId))
                create(owner, TypeNotification.STOCK_DISPONIBLE, "De nouveau disponible",
                        "Le produit « " + productName + " » est de nouveau disponible.",
                        "/produits/" + productId, "available:" + eventKey);
        }
    }

    public void newReview(Integer reviewId, String productName) {
        admins(TypeNotification.NOUVEL_AVIS, "Nouvel avis client",
                "Un nouvel avis a été publié sur « " + productName + " ».",
                "/admin/avis/" + reviewId, "review:" + reviewId);
    }

    public void reviewUpdated(Integer reviewId, String productName) {
        admins(TypeNotification.AVIS_MODIFIE, "Avis client modifié",
                "Un avis sur « " + productName + " » a été modifié et attend votre validation.",
                "/admin/avis/" + reviewId, "review-update:" + reviewId + ":" + UUID.randomUUID());
    }

    private void admins(TypeNotification type, String title, String message, String link, String event) {
        for (Utilisateur admin : users.findByRoleAndActifTrue(Role.ADMINISTRATEUR))
            create(admin.getId(), type, title, message, link, event);
    }
    private void create(Integer recipientId, TypeNotification type, String title, String message, String link, String event) {
        jdbc.update("INSERT INTO notifications(utilisateur_id,type,titre,message,lien,est_lue,date_creation,evenement_cle) " +
                "VALUES(?,?,?,?,?,false,CURRENT_TIMESTAMP,?) ON DUPLICATE KEY UPDATE id=id",
                recipientId, type.name(), title, message, link, event);
    }
    private Integer userId(String email) {
        return users.findByEmail(email).map(Utilisateur::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur introuvable"));
    }
    private NotificationDTO dto(Notification n) {
        return new NotificationDTO(n.getId(), n.getType(), n.getTitre(), n.getMessage(), n.getLien(), n.getEstLue(), n.getDateCreation());
    }
}
