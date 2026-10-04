package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.entity.Commande;
import com.amansia.amansiabackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PanierCommandeService {
    private final PanierRepository paniers;
    private final LignePanierRepository lignes;
    private final LigneCommandeRepository commandes;

    @Transactional
    public void retirerArticlesCommandes(Commande commande) {
        paniers.findByUtilisateurId(commande.getUtilisateur().getId()).ifPresent(panier -> {
            var articles = lignes.findByPanierId(panier.getId());
            for (var achat : commandes.findByCommandeId(commande.getId())) {
                articles.stream().filter(ligne -> Objects.equals(ligne.getProduit().getId(), achat.getProduit().getId())
                    && Objects.equals(ligne.getVariante() == null ? null : ligne.getVariante().getId(),
                                      achat.getVariante() == null ? null : achat.getVariante().getId()))
                    .findFirst().ifPresent(ligne -> {
                        int reste = ligne.getQuantite() - achat.getQuantite();
                        if (reste <= 0) lignes.delete(ligne);
                        else { ligne.setQuantite(reste); lignes.save(ligne); }
                    });
            }
        });
    }
}
