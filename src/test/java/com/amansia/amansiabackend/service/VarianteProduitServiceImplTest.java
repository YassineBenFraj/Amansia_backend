package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.variante.VarianteProduitCreationDTO;
import com.amansia.amansiabackend.entity.Produit;
import com.amansia.amansiabackend.exception.ProduitInactiveException;
import com.amansia.amansiabackend.repository.ProduitRepository;
import com.amansia.amansiabackend.repository.VarianteProduitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VarianteProduitServiceImplTest {

    @Mock
    private VarianteProduitRepository varianteProduitRepository;

    @Mock
    private ProduitRepository produitRepository;

    @InjectMocks
    private VarianteProduitServiceImpl varianteProduitService;

    @Test
    void creerVarianteProduit_shouldRejectInactiveProduct() {
        VarianteProduitCreationDTO dto = new VarianteProduitCreationDTO();
        dto.setProduitId(1);
        dto.setReference("CAN-001");
        dto.setPrixSupplementaire(BigDecimal.valueOf(100));
        dto.setActif(true);

        Produit produit = new Produit();
        produit.setId(1);
        produit.setActif(false);

        when(produitRepository.findById(1)).thenReturn(Optional.of(produit));

        assertThrows(ProduitInactiveException.class, () -> varianteProduitService.creerVarianteProduit(dto));
    }
}
