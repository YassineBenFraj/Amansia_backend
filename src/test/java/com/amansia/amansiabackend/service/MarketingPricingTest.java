package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.entity.*;
import com.amansia.amansiabackend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarketingPricingTest {
    @Mock CodePromoRepository codes;
    @Mock UtilisationCodePromoRepository utilisations;
    @InjectMocks CodePromoService service;

    @Test void productPromotionOnlyDuringItsActivePeriod() {
        Produit product = new Produit(); product.setPrix(new BigDecimal("1500.000"));
        product.setPrixPromotion(new BigDecimal("1200.000")); product.setPromotionActive(true);
        product.setPromotionDateDebut(LocalDateTime.now().minusDays(1));
        product.setPromotionDateFin(LocalDateTime.now().plusDays(1));
        assertEquals(0, PrixProduit.calculer(product, null).compareTo(new BigDecimal("1200")));
        product.setPromotionDateFin(LocalDateTime.now().minusSeconds(1));
        assertEquals(0, PrixProduit.calculer(product, null).compareTo(new BigDecimal("1500")));
        product.setPromotionDateFin(LocalDateTime.now().plusDays(1)); product.setPromotionActive(false);
        assertEquals(0, PrixProduit.calculer(product, null).compareTo(new BigDecimal("1500")));
    }

    @Test void codeIsCaseInsensitiveAndUsesAlreadyPromotedSubtotal() {
        CodePromo code = code(TypeReduction.POURCENTAGE, "10", "300");
        when(codes.findByCode("AMANSIA10")).thenReturn(Optional.of(code));
        var quote = service.quote(" amansia10 ", 7, new BigDecimal("1200.000"));
        assertEquals(0, quote.reduction().compareTo(new BigDecimal("120.000")));
        assertEquals(0, quote.total().compareTo(new BigDecimal("1080.000")));
    }

    @Test void fixedDiscountNeverMakesTotalNegative() {
        CodePromo code = code(TypeReduction.MONTANT_FIXE, "50", "0");
        when(codes.findByCode("BIENVENUE50")).thenReturn(Optional.of(code));
        var quote = service.quote("bienvenue50", 7, new BigDecimal("40.000"));
        assertEquals(0, quote.reduction().compareTo(new BigDecimal("40.000")));
        assertEquals(0, quote.total().compareTo(BigDecimal.ZERO));
    }

    @Test void expiredAndExhaustedCodesAreRejected() {
        CodePromo code = code(TypeReduction.POURCENTAGE, "10", "0");
        when(codes.findByCode("AMANSIA10")).thenReturn(Optional.of(code));
        code.setDateFin(LocalDateTime.now().minusSeconds(1));
        assertTrue(assertThrows(IllegalArgumentException.class,
                () -> service.quote("AMANSIA10", 7, new BigDecimal("100"))).getMessage().contains("expiré"));
        code.setDateFin(LocalDateTime.now().plusDays(1));
        code.setNombreUtilisationsMax(1); code.setNombreUtilisations(1);
        assertTrue(assertThrows(IllegalArgumentException.class,
                () -> service.quote("AMANSIA10", 7, new BigDecimal("100"))).getMessage().contains("limite"));
    }

    @Test void aPaidOrderConsumesItsCodeOnlyOnce() {
        CodePromo code = code(TypeReduction.POURCENTAGE, "10", "0");
        Commande order = new Commande(); order.setId(9); order.setCodePromo("AMANSIA10");
        order.setCodePromoId(1); order.setSousTotal(new BigDecimal("1200.000"));
        when(utilisations.existsByCommandeId(9)).thenReturn(false, true);
        when(codes.lockByCode("AMANSIA10")).thenReturn(Optional.of(code));

        service.consume(order, 7, new BigDecimal("120.000"));
        service.consume(order, 7, new BigDecimal("120.000"));

        assertEquals(1, code.getNombreUtilisations());
        verify(utilisations, times(1)).saveAndFlush(any(UtilisationCodePromo.class));
    }

    private CodePromo code(TypeReduction type, String value, String minimum) {
        CodePromo code = new CodePromo(); code.setId(1);
        code.setCode(type == TypeReduction.POURCENTAGE ? "AMANSIA10" : "BIENVENUE50");
        code.setTypeReduction(type); code.setValeur(new BigDecimal(value));
        code.setMontantMinimum(new BigDecimal(minimum)); code.setActif(true);
        code.setDateDebut(LocalDateTime.now().minusDays(1)); code.setDateFin(LocalDateTime.now().plusDays(1));
        return code;
    }
}
