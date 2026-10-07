package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.repository.ProduitRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@Transactional(readOnly = true)
class CataloguePrixRepositoryTest {
    @Autowired ProduitRepository produits;

    @Test void databasePriceFilterMatchesEffectivePromotionPrice() {
        BigDecimal min = new BigDecimal("500.000");
        BigDecimal max = new BigDecimal("1500.000");
        var expected = produits.findAll().stream()
                .filter(CataloguePublicService::visible)
                .filter(p -> PrixProduit.calculer(p, null).compareTo(min) >= 0)
                .filter(p -> PrixProduit.calculer(p, null).compareTo(max) <= 0)
                .map(p -> p.getId()).collect(Collectors.toSet());
        var actual = produits.rechercherCatalogue(null, null, min, max).stream()
                .map(p -> p.getId()).collect(Collectors.toSet());
        assertEquals(expected, actual);
        var allVisible = produits.findAll().stream().filter(CataloguePublicService::visible)
                .map(p -> p.getId()).collect(Collectors.toSet());
        assertEquals(allVisible, produits.rechercherCatalogue(null, null, null, null).stream()
                .map(p -> p.getId()).collect(Collectors.toSet()));
    }

    @Test void boundsMatchCurrentPublicPrices() {
        var prices = produits.findAll().stream().filter(CataloguePublicService::visible)
                .map(p -> PrixProduit.calculer(p, null)).toList();
        var bounds = produits.bornesPrixCatalogue();
        if (prices.isEmpty()) {
            assertEquals(null, bounds.getMinimum());
            assertEquals(null, bounds.getMaximum());
        } else {
            assertEquals(0, prices.stream().min(BigDecimal::compareTo).orElseThrow().compareTo(bounds.getMinimum()));
            assertEquals(0, prices.stream().max(BigDecimal::compareTo).orElseThrow().compareTo(bounds.getMaximum()));
        }
    }
}
