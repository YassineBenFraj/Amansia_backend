package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.repository.CategorieRepository;
import com.amansia.amansiabackend.repository.ProduitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CataloguePrixTest {
    @Mock ProduitRepository produits;
    @Mock CategorieRepository categories;
    @InjectMocks ProduitServiceImpl service;

    @Test void rejectsNegativeAndInvertedBounds() {
        assertThrows(IllegalArgumentException.class,
                () -> service.rechercherCatalogue(null, null, new BigDecimal("-1"), null));
        assertThrows(IllegalArgumentException.class,
                () -> service.rechercherCatalogue(null, null, new BigDecimal("1500"), new BigDecimal("500")));
        verifyNoInteractions(produits);
    }

    @Test void forwardsIndependentBoundsAndCombinedCriteria() {
        when(produits.rechercherCatalogue("canapé", 2, new BigDecimal("500"), null)).thenReturn(List.of());
        assertTrue(service.rechercherCatalogue(" canapé ", 2, new BigDecimal("500"), null).isEmpty());
        verify(produits).rechercherCatalogue("canapé", 2, new BigDecimal("500"), null);
        when(produits.rechercherCatalogue(null, null, null, new BigDecimal("1500"))).thenReturn(List.of());
        assertTrue(service.rechercherCatalogue(null, null, null, new BigDecimal("1500")).isEmpty());
    }
}
