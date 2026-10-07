package com.amansia.amansiabackend.service;
import com.amansia.amansiabackend.entity.*;
import com.amansia.amansiabackend.repository.*;
import com.amansia.amansiabackend.dto.panier.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
@ExtendWith(MockitoExtension.class)
class PrixPanierTest {
 @Mock PanierRepository paniers;@Mock LignePanierRepository lignes;@Mock ProduitRepository produits;@Mock VarianteProduitRepository variantes;
 @Mock StockService stockService;
 @InjectMocks PanierServiceImpl service;
 @ParameterizedTest @CsvSource({"NULL,0,1000.00","800,0,800.00","800,50,850.00","1200,0,1000.00","-1,0,1000.00","0,0,0.00"})
 void serverCalculatesPrice(String promo,String supplement,String attendu){
  var u=new Utilisateur();u.setId(1);var panier=new Panier();panier.setId(2);var p=new Produit();p.setId(3);p.setActif(true);p.setPrix(new BigDecimal("1000"));p.setPromotionActive(true);if(!promo.equals("NULL"))p.setPrixPromotion(new BigDecimal(promo));
  var dto=new LignePanierCreationDTO();dto.setProduitId(3);dto.setQuantite(1);
  {var v=new VarianteProduit();v.setId(4);v.setProduit(p);v.setActif(true);v.setPrixSupplementaire(new BigDecimal(supplement));dto.setVarianteId(4);when(variantes.findById(4)).thenReturn(Optional.of(v));}
  when(produits.findById(3)).thenReturn(Optional.of(p));when(paniers.findByUtilisateurId(1)).thenReturn(Optional.of(panier));
  List<LignePanier> saved=new ArrayList<>();when(lignes.findByPanierId(2)).thenAnswer(i->List.copyOf(saved));when(lignes.save(any())).thenAnswer(i->{LignePanier l=i.getArgument(0);saved.add(l);return l;});
  assertEquals(0,new BigDecimal(attendu).compareTo(service.ajouterLignePanier(u,dto).getTotalGeneral()));assertEquals(0,new BigDecimal(attendu).compareTo(saved.get(0).getPrixUnitaire()));
 }
 @Test void firstReadCreatesCartInWritableTransaction(){var u=new Utilisateur();u.setId(1);when(paniers.findByUtilisateurId(1)).thenReturn(Optional.empty());when(paniers.save(any())).thenAnswer(i->{Panier p=i.getArgument(0);p.setId(2);return p;});assertEquals(2,service.obtenirPanier(u).getId());verify(paniers).save(any());var source=new org.springframework.transaction.annotation.AnnotationTransactionAttributeSource();assertFalse(source.getTransactionAttribute(findMethod(),PanierServiceImpl.class).isReadOnly());}
 private java.lang.reflect.Method findMethod(){try{return PanierServiceImpl.class.getMethod("obtenirPanier",Utilisateur.class);}catch(Exception e){throw new RuntimeException(e);}}
 @Test void deliveryFeeIsValidated(){assertThrows(IllegalArgumentException.class,()->new FraisLivraisonService(new BigDecimal("-1")));assertEquals(new BigDecimal("9.50"),new FraisLivraisonService(new BigDecimal("9.50")).calculer());}
}
