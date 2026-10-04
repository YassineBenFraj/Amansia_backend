package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.couleur.CouleurCreationDTO;
import com.amansia.amansiabackend.dto.couleur.CouleurDTO;
import com.amansia.amansiabackend.dto.couleur.CouleurUpdateDTO;

import java.util.List;

public interface CouleurService {

    CouleurDTO creerCouleur(CouleurCreationDTO creationDTO);

    CouleurDTO recupererCouleurParId(Integer id);

    List<CouleurDTO> recupererToutesLesCouleurs();

    List<CouleurDTO> recupererCouleursActives();

    List<CouleurDTO> rechercherParNom(String nom);

    CouleurDTO modifierCouleur(Integer id, CouleurUpdateDTO updateDTO);

    void desactiverCouleur(Integer id);
}
