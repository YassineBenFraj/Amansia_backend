package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.adresse.AdresseDTO;
import com.amansia.amansiabackend.dto.adresse.AdresseRequest;
import com.amansia.amansiabackend.entity.Adresse;
import com.amansia.amansiabackend.entity.Utilisateur;
import com.amansia.amansiabackend.exception.AdresseNotFoundException;
import com.amansia.amansiabackend.exception.AdresseUtiliseeException;
import com.amansia.amansiabackend.repository.AdresseRepository;
import com.amansia.amansiabackend.repository.CommandeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdresseService {
    private final AdresseRepository adresses;
    private final CommandeRepository commandes;

    // Le verrou est aussi utilise par le checkout pour eviter une suppression concurrente.
    public Adresse verifierPropriete(Integer id, Integer utilisateurId) {
        return adresses.findOwnedForUpdate(id, utilisateurId).orElseThrow(AdresseNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public List<AdresseDTO> lister(Utilisateur utilisateur) {
        return adresses.findByUtilisateurIdOrderByIdDesc(utilisateur.getId()).stream().map(this::dto).toList();
    }

    public AdresseDTO consulter(Utilisateur utilisateur, Integer id) {
        return dto(verifierPropriete(id, utilisateur.getId()));
    }

    public AdresseDTO creer(Utilisateur utilisateur, AdresseRequest request) {
        Adresse adresse = new Adresse();
        adresse.setUtilisateur(utilisateur);
        appliquer(adresse, request);
        return dto(adresses.save(adresse));
    }

    public AdresseDTO modifier(Utilisateur utilisateur, Integer id, AdresseRequest request) {
        Adresse adresse = verifierPropriete(id, utilisateur.getId());
        verifierNonUtilisee(id);
        appliquer(adresse, request);
        return dto(adresses.save(adresse));
    }

    public void supprimer(Utilisateur utilisateur, Integer id) {
        Adresse adresse = verifierPropriete(id, utilisateur.getId());
        verifierNonUtilisee(id);
        adresses.delete(adresse);
    }

    private void verifierNonUtilisee(Integer id) {
        if (commandes.existsByAdresseLivraisonId(id)) {
            throw new AdresseUtiliseeException();
        }
    }

    private void appliquer(Adresse adresse, AdresseRequest request) {
        adresse.setAdresse(request.adresse());
        adresse.setVille(request.ville());
        adresse.setGouvernorat(request.gouvernorat());
        adresse.setCodePostal(request.codePostal());
        adresse.setPays(request.pays() == null ? "Tunisie" : request.pays());
        adresse.setAdressePrincipale(Boolean.TRUE.equals(request.adressePrincipale()));
    }

    private AdresseDTO dto(Adresse adresse) {
        return new AdresseDTO(adresse.getId(), adresse.getAdresse(), adresse.getVille(),
                adresse.getGouvernorat(), adresse.getCodePostal(), adresse.getPays(),
                adresse.getAdressePrincipale(), adresse.getDateCreation());
    }
}
