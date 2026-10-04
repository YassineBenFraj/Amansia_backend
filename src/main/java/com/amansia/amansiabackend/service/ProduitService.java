package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.produit.ProduitCreationDTO;
import com.amansia.amansiabackend.dto.produit.ProduitDTO;
import com.amansia.amansiabackend.dto.produit.ProduitUpdateDTO;

import java.util.List;

public interface ProduitService {

    ProduitDTO creerProduit(ProduitCreationDTO creationDTO);

    List<ProduitDTO> recupererProduitsActifs();

    List<ProduitDTO> recupererTousLesProduits();

    ProduitDTO recupererProduitParId(Integer id);

    List<ProduitDTO> recupererProduitsParCategorie(Integer categorieId);

    List<ProduitDTO> recupererProduitsActifsParCategorie(Integer categorieId);

    List<ProduitDTO> rechercherParNom(String nom);

    ProduitDTO modifierProduit(Integer id, ProduitUpdateDTO updateDTO);

    void desactiverProduit(Integer id);
}
