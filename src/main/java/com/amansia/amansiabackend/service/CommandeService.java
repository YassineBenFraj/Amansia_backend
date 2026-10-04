package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.commande.CommandeCreationDTO;
import com.amansia.amansiabackend.dto.commande.CommandeDTO;
import com.amansia.amansiabackend.entity.Utilisateur;
import com.amansia.amansiabackend.entity.StatutCommande;

import java.util.List;

public interface CommandeService {
    List<CommandeDTO> recupererTousLesCommandes();

    CommandeDTO creerCommande(Utilisateur utilisateur, CommandeCreationDTO creationDTO);

    List<CommandeDTO> recupererCommandesUtilisateur(Utilisateur utilisateur);

    CommandeDTO recupererCommandeParId(Utilisateur utilisateur, Integer commandeId);

    CommandeDTO annulerCommande(Utilisateur utilisateur, Integer commandeId);
    CommandeDTO recupererCommandeAdmin(Integer commandeId);
    CommandeDTO changerStatutAdmin(Integer commandeId, StatutCommande statut);
}
