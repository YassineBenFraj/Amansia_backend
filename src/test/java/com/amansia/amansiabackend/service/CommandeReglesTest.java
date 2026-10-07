package com.amansia.amansiabackend.service;
import com.amansia.amansiabackend.entity.*;
import com.amansia.amansiabackend.repository.*;
import com.amansia.amansiabackend.dto.commande.*;
import com.amansia.amansiabackend.dto.CodePromoQuote;
import com.amansia.amansiabackend.exception.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
@ExtendWith(MockitoExtension.class)
class CommandeReglesTest {
 @Mock CommandeRepository commandes; @Mock LigneCommandeRepository lignes;
 @Mock PanierRepository paniers; @Mock LignePanierRepository lignesPanier; @Mock ProduitRepository produits;
 @Mock AdresseService adresses; @Mock PaiementRepository paiements; @Mock AdresseRepository adresseRepository;
 @Mock StockService stockService;
 @Mock NotificationService notifications;
 @Mock CodePromoService codes;
 @InjectMocks CommandeServiceImpl service;
 Utilisateur utilisateur(){var u=new Utilisateur();u.setId(1);return u;}
 Commande commande(StatutCommande s){var c=new Commande();c.setId(7);c.setUtilisateur(utilisateur());c.setStatut(s);when(commandes.findByIdForUpdate(7)).thenReturn(Optional.of(c));return c;}
 @Test void checkoutRepricesAndStoresHistoricalAmounts(){
  var u=utilisateur();var p=new Produit();p.setId(3);p.setActif(true);p.setPrix(new BigDecimal("1000"));p.setPrixPromotion(new BigDecimal("800"));p.setPromotionActive(true);
  var v=new VarianteProduit();v.setProduit(p);v.setActif(true);v.setPrixSupplementaire(new BigDecimal("50"));
  var panier=new Panier();panier.setId(2);var lp=new LignePanier();lp.setProduit(p);lp.setVariante(v);lp.setQuantite(2);lp.setPrixUnitaire(BigDecimal.ONE);
  when(paniers.findByUtilisateurId(1)).thenReturn(Optional.of(panier));when(lignesPanier.findByPanierId(2)).thenReturn(List.of(lp));when(produits.findById(3)).thenReturn(Optional.of(p));
  when(codes.quote(isNull(),eq(1),any(BigDecimal.class))).thenAnswer(i->{BigDecimal subtotal=i.getArgument(2);return new CodePromoQuote(null,null,subtotal,BigDecimal.ZERO,subtotal);});
  var dto=new CommandeCreationDTO();dto.setAdresseLivraisonId(8);
  var result=service.creerCommande(u,dto);
  assertEquals(0,new BigDecimal("1700.00").compareTo(result.getSousTotal()));assertEquals(0,new BigDecimal("1700.00").compareTo(result.getTotal()));assertEquals(StatutCommande.EN_ATTENTE,result.getStatut());assertEquals(1,result.getUtilisateurId());
  verify(adresses).verifierPropriete(8,1);verify(lignesPanier,never()).deleteByPanierId(2);
  @SuppressWarnings("unchecked") ArgumentCaptor<Iterable<LigneCommande>> capture=ArgumentCaptor.forClass(Iterable.class);
  verify(lignes).saveAll(capture.capture());var historique=capture.getValue().iterator().next();
  p.setPrix(new BigDecimal("2000"));p.setPrixPromotion(null);v.setPrixSupplementaire(new BigDecimal("100"));
  assertEquals(0,new BigDecimal("850.00").compareTo(historique.getPrixUnitaire()));assertEquals(0,new BigDecimal("1700.00").compareTo(historique.getPrixTotal()));
 }
 @Test void foreignAddressStopsCheckout(){var dto=new CommandeCreationDTO();dto.setAdresseLivraisonId(8);doThrow(new AdresseNotFoundException()).when(adresses).verifierPropriete(8,1);assertThrows(AdresseNotFoundException.class,()->service.creerCommande(utilisateur(),dto));verifyNoInteractions(paniers,commandes);}
 @ParameterizedTest @EnumSource(value=StatutCommande.class,names={"EN_PREPARATION","EXPEDIEE","EN_LIVRAISON","LIVREE","ANNULEE"})
 void advancedOrderCannotBeCancelled(StatutCommande status){commande(status);assertThrows(CommandeAnnulationImpossibleException.class,()->service.annulerCommande(utilisateur(),7));verify(commandes,never()).save(any());}
 @ParameterizedTest @EnumSource(value=StatutPaiement.class,names={"PAYE","REMBOURSE","EN_ATTENTE"})
 void paidOrPendingCardCannotBeCancelled(StatutPaiement status){commande(StatutCommande.EN_ATTENTE);var p=new Paiement();p.setMethode(MethodePaiement.CARTE);p.setStatut(status);when(paiements.findByCommandeId(7)).thenReturn(List.of(p));assertThrows(CommandeAnnulationImpossibleException.class,()->service.annulerCommande(utilisateur(),7));verify(commandes,never()).save(any());}
 @Test void unpaidDeliveryOrderCanBeCancelled(){var c=commande(StatutCommande.CONFIRMEE);var p=new Paiement();p.setMethode(MethodePaiement.PAIEMENT_LIVRAISON);p.setStatut(StatutPaiement.EN_ATTENTE);when(paiements.findByCommandeId(7)).thenReturn(List.of(p));when(commandes.save(c)).thenReturn(c);assertEquals(StatutCommande.ANNULEE,service.annulerCommande(utilisateur(),7).getStatut());}
 @Test void foreignOrderCannotBeCancelled(){var c=commande(StatutCommande.EN_ATTENTE);c.getUtilisateur().setId(9);assertThrows(CommandeAccessDeniedException.class,()->service.annulerCommande(utilisateur(),7));}
 @Test void adminListsAllOrders(){when(commandes.findAll(any(org.springframework.data.domain.Sort.class))).thenReturn(List.of(new Commande(),new Commande()));assertEquals(2,service.recupererTousLesCommandes().size());verify(commandes,never()).findByUtilisateurIdOrderByDateCommandeDesc(any());}
 @Test void adminAdvancesOnlyToNextStatus(){var c=commande(StatutCommande.CONFIRMEE);when(commandes.save(c)).thenReturn(c);assertEquals(StatutCommande.EN_PREPARATION,service.changerStatutAdmin(7,StatutCommande.EN_PREPARATION).getStatut());}
 @Test void adminCannotSkipOrAdvanceCancelledOrder(){commande(StatutCommande.CONFIRMEE);assertThrows(IllegalArgumentException.class,()->service.changerStatutAdmin(7,StatutCommande.EXPEDIEE));clearInvocations(commandes);commande(StatutCommande.ANNULEE);assertThrows(IllegalArgumentException.class,()->service.changerStatutAdmin(7,StatutCommande.LIVREE));}
}
