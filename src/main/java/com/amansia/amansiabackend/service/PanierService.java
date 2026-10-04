package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.panier.LignePanierCreationDTO;
import com.amansia.amansiabackend.dto.panier.LignePanierUpdateDTO;
import com.amansia.amansiabackend.dto.panier.PanierDTO;
import com.amansia.amansiabackend.entity.Utilisateur;

public interface PanierService {

    PanierDTO obtenirPanier(Utilisateur utilisateur);

    PanierDTO ajouterLignePanier(Utilisateur utilisateur, LignePanierCreationDTO creationDTO);

    PanierDTO modifierLignePanier(Utilisateur utilisateur, Integer ligneId, LignePanierUpdateDTO updateDTO);

    void supprimerLignePanier(Utilisateur utilisateur, Integer ligneId);

    void viderPanier(Utilisateur utilisateur);
}
