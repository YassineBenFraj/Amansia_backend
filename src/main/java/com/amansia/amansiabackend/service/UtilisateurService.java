package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.UtilisateurCreationDTO;
import com.amansia.amansiabackend.dto.UtilisateurDTO;
import com.amansia.amansiabackend.dto.UtilisateurUpdateDTO;

import java.util.List;

public interface UtilisateurService {

    UtilisateurDTO creerUtilisateur(UtilisateurCreationDTO utilisateurCreationDTO);

    UtilisateurDTO recupererUtilisateurParId(Integer id);

    List<UtilisateurDTO> recupererTousLesUtilisateurs();

    UtilisateurDTO modifierUtilisateur(Integer id, UtilisateurUpdateDTO utilisateurUpdateDTO, String emailAdministrateur);

    void desactiverUtilisateur(Integer id);

    void supprimerUtilisateur(Integer id, String emailAdministrateur);

    UtilisateurDTO changerStatut(Integer id, Boolean actif, String emailAdministrateur);

    UtilisateurDTO rechercherParEmail(String email);
}
