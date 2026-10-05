package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.UtilisateurCreationDTO;
import com.amansia.amansiabackend.dto.UtilisateurDTO;
import com.amansia.amansiabackend.dto.UtilisateurUpdateDTO;
import com.amansia.amansiabackend.entity.Utilisateur;
import com.amansia.amansiabackend.exception.EmailAlreadyUsedException;
import com.amansia.amansiabackend.exception.UtilisateurNotFoundException;
import com.amansia.amansiabackend.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtilisateurServiceImpl implements UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.amansia.amansiabackend.repository.NotificationRepository notificationRepository;

    @Override
    public UtilisateurDTO creerUtilisateur(UtilisateurCreationDTO utilisateurCreationDTO) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(utilisateurCreationDTO.getNom());
        utilisateur.setPrenom(utilisateurCreationDTO.getPrenom());
        utilisateur.setEmail(utilisateurCreationDTO.getEmail());
        if (utilisateurRepository.existsByEmail(utilisateurCreationDTO.getEmail())) {
            throw new EmailAlreadyUsedException();
        }
        utilisateur.setMotDePasse(passwordEncoder.encode(utilisateurCreationDTO.getMotDePasse()));
        utilisateur.setTelephone(utilisateurCreationDTO.getTelephone());
        utilisateur.setDateNaissance(utilisateurCreationDTO.getDateNaissance());
        utilisateur.setRole(utilisateurCreationDTO.getRole());
        utilisateur.setActif(true);

        utilisateur = utilisateurRepository.save(utilisateur);
        return mapToDTO(utilisateur);
    }

    @Override
    public UtilisateurDTO recupererUtilisateurParId(Integer id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new UtilisateurNotFoundException(id));
        return mapToDTO(utilisateur);
    }

    @Override
    public List<UtilisateurDTO> recupererTousLesUtilisateurs() {
        return utilisateurRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UtilisateurDTO modifierUtilisateur(Integer id, UtilisateurUpdateDTO utilisateurUpdateDTO, String emailAdministrateur) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new UtilisateurNotFoundException(id));
        boolean propreCompte = utilisateur.getEmail().equalsIgnoreCase(emailAdministrateur);
        if (propreCompte && Boolean.FALSE.equals(utilisateurUpdateDTO.getActif())) {
            throw new IllegalArgumentException("Un administrateur ne peut pas désactiver son propre compte");
        }
        if (propreCompte && utilisateurUpdateDTO.getRole() != null
                && utilisateurUpdateDTO.getRole() != utilisateur.getRole()) {
            throw new IllegalArgumentException("Un administrateur ne peut pas modifier son propre rôle");
        }
        if (utilisateurUpdateDTO.getNom() != null) {
            utilisateur.setNom(utilisateurUpdateDTO.getNom());
        }
        if (utilisateurUpdateDTO.getPrenom() != null) {
            utilisateur.setPrenom(utilisateurUpdateDTO.getPrenom());
        }
        if (utilisateurUpdateDTO.getEmail() != null) {
            if (!utilisateur.getEmail().equalsIgnoreCase(utilisateurUpdateDTO.getEmail())
                    && utilisateurRepository.existsByEmail(utilisateurUpdateDTO.getEmail())) {
                throw new EmailAlreadyUsedException();
            }
            utilisateur.setEmail(utilisateurUpdateDTO.getEmail());
        }
        if (utilisateurUpdateDTO.getTelephone() != null) {
            utilisateur.setTelephone(utilisateurUpdateDTO.getTelephone());
        }
        if (utilisateurUpdateDTO.getDateNaissance() != null) {
            utilisateur.setDateNaissance(utilisateurUpdateDTO.getDateNaissance());
        }
        if (utilisateurUpdateDTO.getMotDePasse() != null) {
            utilisateur.setMotDePasse(passwordEncoder.encode(utilisateurUpdateDTO.getMotDePasse()));
        }
        if (utilisateurUpdateDTO.getRole() != null) {
            utilisateur.setRole(utilisateurUpdateDTO.getRole());
        }
        if (utilisateurUpdateDTO.getActif() != null) {
            utilisateur.setActif(utilisateurUpdateDTO.getActif());
        }
        utilisateur = utilisateurRepository.save(utilisateur);
        return mapToDTO(utilisateur);
    }

    @Override
    public void desactiverUtilisateur(Integer id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new UtilisateurNotFoundException(id));
        utilisateur.setActif(false);
        utilisateur.setDateModification(LocalDateTime.now());
        utilisateurRepository.save(utilisateur);
    }

    @Override
    public void supprimerUtilisateur(Integer id, String emailAdministrateur) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new UtilisateurNotFoundException(id));
        if (utilisateur.getEmail().equalsIgnoreCase(emailAdministrateur)) {
            throw new IllegalArgumentException("Un administrateur ne peut pas supprimer son propre compte");
        }
        try {
            utilisateurRepository.delete(utilisateur);
            utilisateurRepository.flush();
            notificationRepository.deleteForUser(id);
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException(
                    "Cet utilisateur possède des données liées et ne peut pas être supprimé. Désactivez son compte.");
        }
    }

    @Override
    public UtilisateurDTO changerStatut(Integer id, Boolean actif, String emailAdministrateur) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new UtilisateurNotFoundException(id));
        if (Boolean.FALSE.equals(actif)
                && utilisateur.getEmail().equalsIgnoreCase(emailAdministrateur)) {
            throw new IllegalArgumentException("Un administrateur ne peut pas désactiver son propre compte");
        }
        utilisateur.setActif(actif);
        utilisateur.setDateModification(LocalDateTime.now());
        return mapToDTO(utilisateurRepository.save(utilisateur));
    }

    @Override
    public UtilisateurDTO rechercherParEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .map(this::mapToDTO)
                .orElseThrow(() -> new UtilisateurNotFoundException("email", email));
    }

    private UtilisateurDTO mapToDTO(Utilisateur utilisateur) {
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setId(utilisateur.getId());
        dto.setNom(utilisateur.getNom());
        dto.setPrenom(utilisateur.getPrenom());
        dto.setEmail(utilisateur.getEmail());
        dto.setTelephone(utilisateur.getTelephone());
        dto.setPhotoProfil(utilisateur.getPhotoProfil());
        dto.setDateNaissance(utilisateur.getDateNaissance());
        dto.setRole(utilisateur.getRole());
        dto.setActif(utilisateur.getActif());
        dto.setDateCreation(utilisateur.getDateCreation());
        dto.setDateModification(utilisateur.getDateModification());
        return dto;
    }
}

