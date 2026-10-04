package com.amansia.amansiabackend.service;
import com.amansia.amansiabackend.entity.*;
import com.amansia.amansiabackend.repository.*;
import com.amansia.amansiabackend.dto.produit.*;
import com.amansia.amansiabackend.dto.image.*;
import com.amansia.amansiabackend.dto.variante.*;
import com.amansia.amansiabackend.exception.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class CataloguePublicServiceTest {
 @Mock ProduitRepository produits;@Mock ImageProduitRepository images;@Mock VarianteProduitRepository variantes;
 @Mock ProduitService produitService;@Mock ImageProduitService imageService;@Mock VarianteProduitService varianteService;
 @Mock StockService stockService;
    @InjectMocks CataloguePublicService service;
 Produit produit(boolean actif,boolean categorieActive){var p=new Produit();p.setId(1);p.setActif(actif);var c=new Categorie();c.setActif(categorieActive);p.setCategorie(c);return p;}
 @Test void inactiveProductOrCategoryCannotBeRead(){when(produits.findById(1)).thenReturn(Optional.of(produit(false,true))).thenReturn(Optional.of(produit(true,false)));assertThrows(ProduitNotFoundException.class,()->service.produit(1));assertThrows(ProduitNotFoundException.class,()->service.produit(1));verifyNoInteractions(produitService);}
 @Test void publicListFiltersInactiveResources(){var dto=new ProduitDTO();dto.setId(1);when(produitService.recupererProduitsActifs()).thenReturn(List.of(dto));when(produits.findById(1)).thenReturn(Optional.of(produit(true,false)));assertTrue(service.produits().isEmpty());}
 @Test void imageMetadataAndFileAreHiddenForInactiveProduct(){var i=new ImageProduit();i.setProduit(produit(false,true));when(images.findByUrl("/uploads/produits/a.jpg")).thenReturn(List.of(i));assertFalse(service.fichierVisible("/uploads/produits/a.jpg"));var dto=new ImageProduitDTO();dto.setProduitId(1);when(imageService.recupererImageParId(2)).thenReturn(dto);when(produits.findById(1)).thenReturn(Optional.of(i.getProduit()));assertThrows(ImageProduitNotFoundException.class,()->service.image(2));}
 @Test void variantWithInactiveMaterialIsHidden(){var p=produit(true,true);when(produits.findById(1)).thenReturn(Optional.of(p));var dto=new VarianteProduitDTO();dto.setId(2);when(varianteService.recupererVariantesProduitActivesParProduit(1)).thenReturn(List.of(dto));var v=new VarianteProduit();v.setProduit(p);v.setActif(true);var m=new Materiau();m.setActif(false);v.setMateriau(m);when(variantes.findById(2)).thenReturn(Optional.of(v));assertTrue(service.variantes(1).isEmpty());}
}
