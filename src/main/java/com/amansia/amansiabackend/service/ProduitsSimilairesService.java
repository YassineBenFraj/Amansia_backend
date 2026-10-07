package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.produit.ProduitDTO;
import com.amansia.amansiabackend.dto.produit.ProduitSimilaireDTO;
import com.amansia.amansiabackend.exception.ProduitNotFoundException;
import com.amansia.amansiabackend.repository.ImageProduitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProduitsSimilairesService {
    private final JdbcTemplate jdbc;
    private final ProduitService produits;
    private final StockService stocks;
    private final ImageProduitRepository images;

    public List<ProduitSimilaireDTO> similaires(Integer produitId) {
        List<Candidat> source = jdbc.query(
            "SELECT p.id,p.categorie_id,CASE WHEN p.promotion_active=1 AND p.prix_promotion>=0 AND p.prix_promotion<p.prix AND (p.promotion_date_debut IS NULL OR p.promotion_date_debut<=NOW()) AND (p.promotion_date_fin IS NULL OR p.promotion_date_fin>=NOW()) THEN p.prix_promotion ELSE p.prix END prix " +
            "FROM produits p JOIN categories c ON c.id=p.categorie_id " +
            "WHERE p.id=? AND p.actif=1 AND c.actif=1",
            (rs, row) -> new Candidat(rs.getInt("id"),rs.getInt("categorie_id"),rs.getBigDecimal("prix")), produitId);
        if (source.isEmpty()) throw new ProduitNotFoundException(produitId);
        Candidat cible = source.get(0);
        List<Candidat> candidats = jdbc.query(
            "SELECT p.id,p.categorie_id,CASE WHEN p.promotion_active=1 AND p.prix_promotion>=0 AND p.prix_promotion<p.prix AND (p.promotion_date_debut IS NULL OR p.promotion_date_debut<=NOW()) AND (p.promotion_date_fin IS NULL OR p.promotion_date_fin>=NOW()) THEN p.prix_promotion ELSE p.prix END prix " +
            "FROM produits p JOIN categories c ON c.id=p.categorie_id " +
            "WHERE p.id<>? AND p.actif=1 AND c.actif=1",
            (rs, row) -> new Candidat(rs.getInt("id"),rs.getInt("categorie_id"),rs.getBigDecimal("prix")), produitId);
        Map<Integer, Options> options = optionsActives();
        Map<Integer, Integer> disponibles = stocks.disponibiliteProduits();
        Options optionsCible = options.getOrDefault(produitId, new Options());
        return candidats.stream()
            .sorted(Comparator
                .comparing((Candidat c) -> disponibles.getOrDefault(c.id(),0)>0).reversed()
                .thenComparing(c -> c.categorieId().equals(cible.categorieId()), Comparator.reverseOrder())
                .thenComparing(Comparator.comparingInt((Candidat c) -> score(cible,c,optionsCible,options.get(c.id()))).reversed())
                .thenComparing(Candidat::id))
            .limit(4)
            .map(c -> {
                ProduitDTO dto = produits.recupererProduitParId(c.id());
                dto.setDisponible(disponibles.getOrDefault(c.id(),0)>0);
                String image = images.findByProduitIdOrderByOrdreAsc(c.id()).stream()
                    .sorted(Comparator.comparing((com.amansia.amansiabackend.entity.ImageProduit i) -> Boolean.TRUE.equals(i.getPrincipale())).reversed()
                        .thenComparing(com.amansia.amansiabackend.entity.ImageProduit::getOrdre))
                    .map(com.amansia.amansiabackend.entity.ImageProduit::getUrl).findFirst().orElse(null);
                return new ProduitSimilaireDTO(dto,image);
            }).toList();
    }

    private Map<Integer, Options> optionsActives() {
        Map<Integer, Options> result = new HashMap<>();
        jdbc.query("SELECT v.produit_id,v.materiau_id,v.couleur_id FROM variantes_produits v " +
            "LEFT JOIN materiaux m ON m.id=v.materiau_id LEFT JOIN couleurs c ON c.id=v.couleur_id " +
            "WHERE v.actif=1 AND (m.id IS NULL OR m.actif=1) AND (c.id IS NULL OR c.actif=1)", rs -> {
            Options options = result.computeIfAbsent(rs.getInt(1), ignored -> new Options());
            int materiauId = rs.getInt(2); if (!rs.wasNull()) options.materiaux.add(materiauId);
            int couleurId = rs.getInt(3); if (!rs.wasNull()) options.couleurs.add(couleurId);
        });
        return result;
    }

    static int score(Candidat source, Candidat candidat, Options sourceOptions, Options candidateOptions) {
        int score = 0;
        if (source.categorieId().equals(candidat.categorieId())) score += 100;
        if (candidateOptions != null) {
            if (sourceOptions.materiaux.stream().anyMatch(candidateOptions.materiaux::contains)) score += 20;
            if (sourceOptions.couleurs.stream().anyMatch(candidateOptions.couleurs::contains)) score += 20;
        }
        if (source.prix().signum()>0 && candidat.prix().subtract(source.prix()).abs()
            .compareTo(source.prix().multiply(new BigDecimal("0.20")))<=0) score += 5;
        return score;
    }

    record Candidat(Integer id, Integer categorieId, BigDecimal prix) {}
    static final class Options {
        final Set<Integer> materiaux = new HashSet<>();
        final Set<Integer> couleurs = new HashSet<>();
    }
}
