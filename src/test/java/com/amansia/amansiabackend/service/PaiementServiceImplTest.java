package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.paiement.PaiementCreationDTO;
import com.amansia.amansiabackend.dto.paiement.PaiementDTO;
import com.amansia.amansiabackend.dto.CodePromoQuote;
import com.amansia.amansiabackend.dto.panier.PanierDTO;
import com.amansia.amansiabackend.dto.panier.LignePanierDTO;
import com.amansia.amansiabackend.dto.variante.VarianteProduitDTO;
import com.amansia.amansiabackend.dto.commande.CommandeCreationDTO;
import com.amansia.amansiabackend.dto.commande.CommandeDTO;
import com.amansia.amansiabackend.dto.commande.LigneCommandeDTO;
import com.amansia.amansiabackend.entity.Commande;
import com.amansia.amansiabackend.entity.MethodePaiement;
import com.amansia.amansiabackend.entity.StatutCommande;
import com.amansia.amansiabackend.entity.StatutPaiement;
import com.amansia.amansiabackend.entity.Utilisateur;
import com.amansia.amansiabackend.exception.CommandeAccessDeniedException;
import com.amansia.amansiabackend.repository.CommandeRepository;
import com.amansia.amansiabackend.repository.PaiementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaiementServiceImplTest {

    @Mock
    private PaiementRepository paiementRepository;

    @Mock
    private CommandeRepository commandeRepository;

    @Mock
    private StripePaymentService stripePaymentService;
    @Mock private PanierCommandeService panierCommandeService;
    @Mock private NotificationService notifications;
    @Mock private PanierService panierService;
    @Mock private StockService stockService;
    @Mock private CodePromoService codes;
    @Mock private CommandeService commandeService;

    @InjectMocks
    private PaiementServiceImpl paiementService;

    @Test
    void creerPaiement_shouldUseOrderTotalAndRemainPending() {
        Utilisateur utilisateur = utilisateur(1);
        Commande commande = commande(15, utilisateur);
        PaiementCreationDTO creationDTO = new PaiementCreationDTO();
        creationDTO.setCommandeId(15);
        creationDTO.setMethode(MethodePaiement.PAIEMENT_LIVRAISON);

        when(commandeRepository.findByIdForUpdate(15)).thenReturn(Optional.of(commande));
        when(paiementRepository.findByCommandeId(15)).thenReturn(List.of());
        when(paiementRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));

        PaiementDTO paiement = paiementService.creerPaiement(utilisateur, creationDTO);

        assertEquals(new BigDecimal("125.50"), paiement.getMontant());
        assertEquals(StatutPaiement.EN_ATTENTE, paiement.getStatut());
        assertEquals(MethodePaiement.PAIEMENT_LIVRAISON, paiement.getMethode());
        assertNotNull(paiement.getReference());
        verify(paiementRepository).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void creerPaiement_shouldRejectAnotherUsersOrder() {
        Utilisateur proprietaire = utilisateur(1);
        Utilisateur autreUtilisateur = utilisateur(2);
        Commande commande = commande(15, proprietaire);
        PaiementCreationDTO creationDTO = new PaiementCreationDTO();
        creationDTO.setCommandeId(15);
        creationDTO.setMethode(MethodePaiement.CARTE);

        when(commandeRepository.findByIdForUpdate(15)).thenReturn(Optional.of(commande));

        assertThrows(CommandeAccessDeniedException.class,
                () -> paiementService.creerPaiement(autreUtilisateur, creationDTO));
    }

    @Test
    void creerPaiementByCard_shouldReturnClientSecret() {
        Utilisateur utilisateur = utilisateur(1);
        Commande commande = commande(15, utilisateur);
        PaiementCreationDTO creationDTO = new PaiementCreationDTO();
        creationDTO.setCommandeId(15);
        creationDTO.setMethode(MethodePaiement.CARTE);

        when(commandeRepository.findByIdForUpdate(15)).thenReturn(Optional.of(commande));
        when(paiementRepository.findByCommandeId(15)).thenReturn(List.of());
        when(paiementRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(stripePaymentService.creerPaymentIntent(org.mockito.ArgumentMatchers.any())).thenReturn("pi_secret_test");

        PaiementDTO paiement = paiementService.creerPaiement(utilisateur, creationDTO);

        assertEquals("pi_secret_test", paiement.getClientSecret());
        assertEquals(StatutPaiement.EN_ATTENTE, paiement.getStatut());
    }

    @Test
    void stripePreparationUsesServerCouponTotal() {
        Utilisateur user = utilisateur(1);
        PanierDTO cart = new PanierDTO();
        cart.setTotalGeneral(new BigDecimal("1200.000"));
        LignePanierDTO line = new LignePanierDTO(); line.setQuantite(1);
        VarianteProduitDTO variant = new VarianteProduitDTO(); variant.setId(5); line.setVariante(variant);
        cart.setLignes(List.of(line));
        when(panierService.obtenirPanier(user)).thenReturn(cart);
        when(codes.quote("AMANSIA10", 1, new BigDecimal("1200.000")))
                .thenReturn(new CodePromoQuote(3, "AMANSIA10", new BigDecimal("1200.000"), new BigDecimal("120.000"), new BigDecimal("1080.000")));
        when(stripePaymentService.preparerAutorisation(org.mockito.ArgumentMatchers.eq(new BigDecimal("1080.000")),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq(1))).thenReturn("secret");

        var prepared = paiementService.preparerPaiementCarte(user, "AMANSIA10");

        assertEquals(new BigDecimal("1080.000"), prepared.total());
        assertEquals("secret", prepared.clientSecret());
        verify(stockService).verifier(5, 1);
    }

    @Test
    void fullyDiscountedOrderIsConfirmedWithoutStripe() {
        Utilisateur user = utilisateur(1);
        PanierDTO cart = new PanierDTO(); cart.setTotalGeneral(new BigDecimal("40.000"));
        cart.setLignes(List.of(new LignePanierDTO()));
        CommandeCreationDTO request = new CommandeCreationDTO(); request.setAdresseLivraisonId(2); request.setCodePromo("BIENVENUE50");
        CommandeDTO created = new CommandeDTO(); created.setId(9);
        LigneCommandeDTO line = new LigneCommandeDTO(); line.setVarianteId(5); line.setQuantite(1); created.setLignes(List.of(line));
        Commande order = commande(9, user); order.setTotal(BigDecimal.ZERO); order.setRemise(new BigDecimal("40.000")); order.setNumeroCommande("CMD-9");
        when(panierService.obtenirPanier(user)).thenReturn(cart);
        when(codes.lockedQuote("BIENVENUE50", 1, new BigDecimal("40.000")))
                .thenReturn(new CodePromoQuote(3, "BIENVENUE50", new BigDecimal("40.000"), new BigDecimal("40.000"), BigDecimal.ZERO));
        when(commandeService.creerCommande(user, request)).thenReturn(created);
        when(commandeRepository.findByIdForUpdate(9)).thenReturn(Optional.of(order));
        when(commandeService.recupererCommandeParId(user, 9)).thenReturn(created);

        assertEquals(9, paiementService.finaliserCommandeOfferte(user, request).getId());
        verify(stockService).vendre(5, 1, order.getNumeroCommande());
        verify(codes).consume(order, 1, new BigDecimal("40.000"));
        org.mockito.Mockito.verifyNoInteractions(stripePaymentService);
    }

    private Utilisateur utilisateur(Integer id) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(id);
        return utilisateur;
    }

    private Commande commande(Integer id, Utilisateur utilisateur) {
        Commande commande = new Commande();
        commande.setId(id);
        commande.setUtilisateur(utilisateur);
        commande.setTotal(new BigDecimal("125.50"));
        commande.setStatut(StatutCommande.EN_ATTENTE);
        return commande;
    }
}
