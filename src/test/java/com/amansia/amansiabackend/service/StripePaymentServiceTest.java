package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.entity.Paiement;
import com.amansia.amansiabackend.entity.StatutPaiement;
import com.amansia.amansiabackend.exception.StripeWebhookInvalidException;
import com.amansia.amansiabackend.repository.CommandeRepository;
import com.amansia.amansiabackend.repository.PaiementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StripePaymentServiceTest {
    private static final String SECRET = "whsec_test_secret";
    private static final String REFERENCE = "PAY-TEST-1";
    @Mock PaiementRepository paiements;
    @Mock CommandeRepository commandes;
    @Mock PanierCommandeService panier;
    private StripePaymentService service;

    @BeforeEach void setUp() {
        service = new StripePaymentService(paiements, commandes, panier);
        ReflectionTestUtils.setField(service, "webhookSecret", SECRET);
        ReflectionTestUtils.setField(service, "currency", "tnd");
    }

    @Test void succeededIntentMarksPaymentPaid() throws Exception {
        Paiement paiement = new Paiement();
        paiement.setReference(REFERENCE);
        paiement.setStatut(StatutPaiement.EN_ATTENTE);
        preparer(paiement);
        when(paiements.findByReferenceForUpdate(REFERENCE)).thenReturn(Optional.of(paiement));
        String body = payload("payment_intent.succeeded");
        service.traiterWebhook(body, signature(body));
        assertEquals(StatutPaiement.PAYE, paiement.getStatut());
        assertNotNull(paiement.getDatePaiement());
        verify(paiements).save(paiement);
        verify(panier).retirerArticlesCommandes(paiement.getCommande());
        assertEquals(com.amansia.amansiabackend.entity.StatutCommande.CONFIRMEE, paiement.getCommande().getStatut());
    }

    @Test void duplicateSucceededIntentIsIdempotent() throws Exception {
        Paiement paiement = new Paiement();
        paiement.setReference(REFERENCE);
        paiement.setStatut(StatutPaiement.PAYE);
        preparer(paiement);
        when(paiements.findByReferenceForUpdate(REFERENCE)).thenReturn(Optional.of(paiement));
        String body = payload("payment_intent.succeeded");
        service.traiterWebhook(body, signature(body));
        verify(paiements, never()).save(paiement);
        verify(panier, never()).retirerArticlesCommandes(org.mockito.ArgumentMatchers.any());
    }

    @Test void invalidSignatureIsRejected() {
        String body = payload("payment_intent.succeeded");
        assertThrows(StripeWebhookInvalidException.class, () -> service.traiterWebhook(body, "t=1,v1=invalid"));
        verify(paiements, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test void wrongAmountDoesNotPayOrRemoveCart() throws Exception {
        Paiement paiement = new Paiement(); paiement.setReference(REFERENCE); paiement.setStatut(StatutPaiement.EN_ATTENTE);
        preparer(paiement);
        when(paiements.findByReferenceForUpdate(REFERENCE)).thenReturn(Optional.of(paiement));
        String body = payload("payment_intent.succeeded").replace("10000", "1");
        assertThrows(StripeWebhookInvalidException.class, () -> service.traiterWebhook(body, signature(body)));
        assertEquals(StatutPaiement.EN_ATTENTE, paiement.getStatut());
        verify(panier, never()).retirerArticlesCommandes(org.mockito.ArgumentMatchers.any());
    }

    private String payload(String type) {
        return "{\"id\":\"evt_test\",\"object\":\"event\",\"data\":{\"object\":{" +
                "\"id\":\"pi_test\",\"amount\":10000,\"currency\":\"tnd\",\"status\":\"succeeded\",\"object\":\"payment_intent\",\"metadata\":{\"payment_reference\":\"" + REFERENCE + "\"}}},\"type\":\"" + type + "\"}";
    }

    private void preparer(Paiement paiement) {
        var commande = new com.amansia.amansiabackend.entity.Commande();
        commande.setId(1); commande.setStatut(com.amansia.amansiabackend.entity.StatutCommande.EN_ATTENTE);
        paiement.setCommande(commande); paiement.setMontant(new java.math.BigDecimal("10"));
        paiement.setMethode(com.amansia.amansiabackend.entity.MethodePaiement.CARTE);
        when(paiements.findCommandeIdByReference(REFERENCE)).thenReturn(Optional.of(1));
        when(commandes.findByIdForUpdate(1)).thenReturn(Optional.of(commande));
    }

    private String signature(String payload) throws Exception {
        long timestamp = Instant.now().getEpochSecond();
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String digest = HexFormat.of().formatHex(mac.doFinal((timestamp + "." + payload).getBytes(StandardCharsets.UTF_8)));
        return "t=" + timestamp + ",v1=" + digest;
    }
}
