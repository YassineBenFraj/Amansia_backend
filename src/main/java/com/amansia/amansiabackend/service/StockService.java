package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.stock.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockService {
    private final JdbcTemplate jdbc;
    private final NotificationService notifications;

    @Transactional(readOnly = true)
    public java.util.Map<Integer,Integer> disponibiliteProduits() {
        return jdbc.query("SELECT v.produit_id,SUM(COALESCE(s.quantite,0)) AS total FROM variantes_produits v " +
            "LEFT JOIN stocks s ON s.variante_id=v.id LEFT JOIN couleurs c ON c.id=v.couleur_id " +
            "LEFT JOIN materiaux ma ON ma.id=v.materiau_id WHERE v.actif=1 " +
            "AND (c.id IS NULL OR c.actif=1) AND (ma.id IS NULL OR ma.actif=1) GROUP BY v.produit_id",
            rs -> { var result=new java.util.HashMap<Integer,Integer>(); while(rs.next()) result.put(rs.getInt(1),rs.getInt(2)); return result; });
    }

    @Transactional(readOnly = true)
    public List<StockDTO> stocks() { return lireStocks(null); }

    @Transactional(readOnly = true)
    public List<StockDTO> stocksProduit(Integer produitId) { return lireStocks(produitId); }

    @Transactional(readOnly = true)
    public List<StockDTO> stocksProduitPublic(Integer produitId) { return lireStocks(produitId).stream().filter(stock -> {
        Integer visible=jdbc.queryForObject("SELECT COUNT(*) FROM variantes_produits v LEFT JOIN couleurs c ON c.id=v.couleur_id LEFT JOIN materiaux m ON m.id=v.materiau_id WHERE v.id=? AND v.actif=1 AND (c.id IS NULL OR c.actif=1) AND (m.id IS NULL OR m.actif=1)",Integer.class,stock.varianteId());
        return visible!=null && visible>0;
    }).toList(); }

    private List<StockDTO> lireStocks(Integer produitId) {
        String sql = "SELECT v.id variante_id,v.reference,v.produit_id,p.nom produit_nom,c.nom couleur,ma.nom materiau," +
            "COALESCE(s.quantite,0) quantite,COALESCE(s.seuil_alerte,5) seuil_alerte,s.date_mise_a_jour " +
            "FROM variantes_produits v JOIN produits p ON p.id=v.produit_id " +
            "LEFT JOIN couleurs c ON c.id=v.couleur_id LEFT JOIN materiaux ma ON ma.id=v.materiau_id " +
            "LEFT JOIN stocks s ON s.variante_id=v.id" + (produitId == null ? "" : " WHERE v.produit_id=?") + " ORDER BY p.nom,v.reference";
        var mapper = (org.springframework.jdbc.core.RowMapper<StockDTO>) (rs, row) ->
            new StockDTO(rs.getInt("variante_id"),rs.getString("reference"),rs.getInt("produit_id"),
                rs.getString("produit_nom"),rs.getString("couleur"),rs.getString("materiau"),
                rs.getInt("quantite"),rs.getInt("seuil_alerte"),
                rs.getTimestamp("date_mise_a_jour") == null ? null : rs.getTimestamp("date_mise_a_jour").toLocalDateTime());
        return produitId == null ? jdbc.query(sql, mapper) : jdbc.query(sql, mapper, produitId);
    }

    @Transactional(readOnly = true)
    public List<MouvementStockDTO> mouvements() {
        return jdbc.query("SELECT m.id,m.variante_id,p.nom produit_nom,v.reference reference_variante," +
                "m.type_mouvement,m.quantite,m.motif,m.reference,m.date_mouvement " +
                "FROM mouvements_stocks m JOIN variantes_produits v ON v.id=m.variante_id " +
                "JOIN produits p ON p.id=v.produit_id ORDER BY m.date_mouvement DESC,m.id DESC",
            (rs, row) -> new MouvementStockDTO(rs.getInt("id"),rs.getInt("variante_id"),
                rs.getString("produit_nom"),rs.getString("reference_variante"),rs.getString("type_mouvement"),
                rs.getInt("quantite"),rs.getString("motif"),rs.getString("reference"),
                rs.getTimestamp("date_mouvement").toLocalDateTime()));
    }

    @Transactional(readOnly = true)
    public void verifier(Integer varianteId, Integer quantite) {
        if (varianteId == null) throw new IllegalArgumentException("Choisissez une variante disponible.");
        if (quantite == null || quantite < 1) throw new IllegalArgumentException("Quantité invalide.");
        Integer disponible = jdbc.query("SELECT quantite FROM stocks WHERE variante_id=?",
            rs -> rs.next() ? rs.getInt(1) : null, varianteId);
        if (disponible == null || disponible < quantite)
            throw new IllegalArgumentException("Stock insuffisant pour cette variante (" + (disponible == null ? 0 : disponible) + " disponible(s)).");
    }

    @Transactional
    public StockDTO mouvementAdmin(MouvementStockRequest request, Integer utilisateurId) {
        if (!List.of("ENTREE", "SORTIE", "AJUSTEMENT").contains(request.typeMouvement()))
            throw new IllegalArgumentException("Type de mouvement non autorisé.");
        if (request.varianteId() == null || request.quantite() == null || request.quantite() < 1 || request.motif() == null || request.motif().isBlank())
            throw new IllegalArgumentException("Variante, quantité et motif sont obligatoires.");
        Integer exists = jdbc.queryForObject("SELECT COUNT(*) FROM variantes_produits WHERE id=?",Integer.class,request.varianteId());
        if (exists == null || exists == 0) throw new IllegalArgumentException("Variante introuvable.");
        StockDTO before = variant(request.varianteId());
        boolean availableBefore = publicProductAvailable(before.produitId());
        if (request.typeMouvement().equals("SORTIE")) sortir(request.varianteId(),request.quantite());
        else entrer(request.varianteId(),request.quantite());
        tracer(request.varianteId(),request.typeMouvement(),request.quantite(),request.motif().trim(),null,utilisateurId);
        StockDTO after = variant(request.varianteId());
        boolean availableAfter = publicProductAvailable(after.produitId());
        notifications.stockChanged(after.produitId(), after.produitNom(), after.varianteId(), after.reference(),
                before.quantite(), after.quantite(), after.seuilAlerte(), availableBefore, availableAfter, UUID.randomUUID().toString());
        return after;
    }

    @Transactional
    public void vendre(Integer varianteId, Integer quantite, String numeroCommande) {
        // A reference is unique per order/variant; the enclosing payment transaction prevents duplicate checkout.
        Integer deja = jdbc.queryForObject("SELECT COUNT(*) FROM mouvements_stocks WHERE variante_id=? AND type_mouvement='SORTIE' AND reference=?",Integer.class,varianteId,numeroCommande);
        if (deja != null && deja > 0) return;
        StockDTO before = variant(varianteId);
        sortir(varianteId,quantite);
        tracer(varianteId,"SORTIE",quantite,"Vente commande",numeroCommande,null);
        StockDTO after = variant(varianteId);
        notifications.stockChanged(after.produitId(), after.produitNom(), after.varianteId(), after.reference(),
                before.quantite(), after.quantite(), after.seuilAlerte(), true, true, "sale:" + numeroCommande + ":" + varianteId);
    }

    private void sortir(Integer varianteId, Integer quantite) {
        if (jdbc.update("UPDATE stocks SET quantite=quantite-? WHERE variante_id=? AND quantite>=?",quantite,varianteId,quantite) != 1)
            throw new IllegalArgumentException("Stock insuffisant pour cette variante.");
    }

    private void entrer(Integer varianteId, Integer quantite) {
        jdbc.update("INSERT INTO stocks(variante_id,quantite,seuil_alerte) VALUES(?,?,5) ON DUPLICATE KEY UPDATE quantite=quantite+VALUES(quantite)",varianteId,quantite);
    }

    private void tracer(Integer varianteId, String type, Integer quantite, String motif, String reference, Integer utilisateurId) {
        jdbc.update("INSERT INTO mouvements_stocks(variante_id,type_mouvement,quantite,motif,reference,utilisateur_id) VALUES(?,?,?,?,?,?)",
            varianteId,type,quantite,motif,reference,utilisateurId);
    }

    private StockDTO variant(Integer id) {
        Integer productId = jdbc.queryForObject("SELECT produit_id FROM variantes_produits WHERE id=?", Integer.class, id);
        return lireStocks(productId).stream().filter(item -> item.varianteId().equals(id)).findFirst().orElseThrow();
    }

    private boolean publicProductAvailable(Integer productId) {
        Boolean active = jdbc.queryForObject("SELECT actif FROM produits WHERE id=?", Boolean.class, productId);
        return Boolean.TRUE.equals(active) && disponibiliteProduits().getOrDefault(productId, 0) > 0;
    }
}
