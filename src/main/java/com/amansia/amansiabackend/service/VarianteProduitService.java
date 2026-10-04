package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.variante.VarianteProduitCreationDTO;
import com.amansia.amansiabackend.dto.variante.VarianteProduitDTO;
import com.amansia.amansiabackend.dto.variante.VarianteProduitUpdateDTO;

import java.util.List;

public interface VarianteProduitService {

    VarianteProduitDTO creerVarianteProduit(VarianteProduitCreationDTO creationDTO);

    VarianteProduitDTO recupererVarianteProduitParId(Integer id);

    List<VarianteProduitDTO> recupererToutesLesVariantesProduit();

    List<VarianteProduitDTO> recupererVariantesProduitParProduit(Integer produitId);

    List<VarianteProduitDTO> recupererVariantesProduitActivesParProduit(Integer produitId);

    List<VarianteProduitDTO> rechercherParReference(String reference);

    VarianteProduitDTO modifierVarianteProduit(Integer id, VarianteProduitUpdateDTO updateDTO);

    void desactiverVarianteProduit(Integer id);
}
