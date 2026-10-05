package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.avis.AvisCreationDTO;
import com.amansia.amansiabackend.dto.avis.AvisDTO;
import com.amansia.amansiabackend.dto.avis.AvisUpdateDTO;
import com.amansia.amansiabackend.entity.Utilisateur;

import java.util.List;

public interface AvisService {

    AvisDTO creerAvis(Utilisateur utilisateur, AvisCreationDTO creationDTO);

    List<AvisDTO> recupererAvisProduit(Integer produitId, boolean administrateur, Integer utilisateurId);

    List<AvisDTO> recupererTousAvis();

    AvisDTO recupererAvis(Integer avisId, Utilisateur utilisateur, boolean administrateur);

    AvisDTO modifierAvis(Integer avisId, Utilisateur utilisateur, AvisUpdateDTO updateDTO, boolean administrateur);

    AvisDTO modererAvis(Integer avisId, boolean approuve);

    void supprimerAvis(Integer avisId, Utilisateur utilisateur, boolean administrateur);
}
