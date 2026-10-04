package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.produit.ProduitSimilaireDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProduitsSimilairesServiceTest {
    @Autowired private ProduitsSimilairesService service;
    @Autowired private JdbcTemplate jdbc;

    @Test void returnsAtMostFourOtherPublicProducts() {
        Integer id = jdbc.queryForObject("SELECT p.id FROM produits p JOIN categories c ON c.id=p.categorie_id WHERE p.actif=1 AND c.actif=1 LIMIT 1", Integer.class);
        List<ProduitSimilaireDTO> results = service.similaires(id);
        assertTrue(results.size() <= 4);
        assertTrue(results.stream().noneMatch(result -> result.produit().getId().equals(id)));
        assertTrue(results.stream().allMatch(result -> Boolean.TRUE.equals(result.produit().getActif())));
        assertTrue(results.stream().allMatch(result -> result.produit().getDisponible() != null));
        boolean ruptureVue = false;
        for (var result : results) {
            if (Boolean.FALSE.equals(result.produit().getDisponible())) ruptureVue = true;
            else assertFalse(ruptureVue, "Un produit disponible doit précéder les ruptures");
        }
    }

    @Test void categoryOutweighsOtherSignals() {
        var source = new ProduitsSimilairesService.Candidat(1,1,new BigDecimal("1000"));
        var sameCategory = new ProduitsSimilairesService.Candidat(2,1,new BigDecimal("3000"));
        var otherCategory = new ProduitsSimilairesService.Candidat(3,2,new BigDecimal("1000"));
        var options = new ProduitsSimilairesService.Options();
        assertTrue(ProduitsSimilairesService.score(source,sameCategory,options,options)
            > ProduitsSimilairesService.score(source,otherCategory,options,options));
    }
}
