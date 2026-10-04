package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.couleur.CouleurCreationDTO;
import com.amansia.amansiabackend.dto.couleur.CouleurDTO;
import com.amansia.amansiabackend.dto.couleur.CouleurUpdateDTO;
import com.amansia.amansiabackend.entity.Couleur;
import com.amansia.amansiabackend.exception.CodeHexadecimalInvalideException;
import com.amansia.amansiabackend.exception.CouleurNameAlreadyUsedException;
import com.amansia.amansiabackend.exception.CouleurNotFoundException;
import com.amansia.amansiabackend.repository.CouleurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CouleurServiceImpl implements CouleurService {

    private final CouleurRepository couleurRepository;

    @Override
    public CouleurDTO creerCouleur(CouleurCreationDTO creationDTO) {
        String nom = normaliserNom(creationDTO.getNom());
        verifierNomDisponible(nom);
        String codeHexadecimal = normaliserCodeHexadecimal(creationDTO.getCodeHexadecimal());

        Couleur couleur = new Couleur();
        couleur.setNom(nom);
        couleur.setCodeHexadecimal(codeHexadecimal);
        couleur.setActif(creationDTO.getActif() != null ? creationDTO.getActif() : true);
        return convertirEnDTO(couleurRepository.save(couleur));
    }

    @Override
    @Transactional(readOnly = true)
    public CouleurDTO recupererCouleurParId(Integer id) {
        return convertirEnDTO(trouverCouleur(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CouleurDTO> recupererToutesLesCouleurs() {
        return couleurRepository.findAll().stream()
                .map(this::convertirEnDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CouleurDTO> recupererCouleursActives() {
        return couleurRepository.findByActifTrue().stream()
                .map(this::convertirEnDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CouleurDTO> rechercherParNom(String nom) {
        String nomRecherche = normaliserNom(nom);
        return couleurRepository.findByNomContainingIgnoreCase(nomRecherche).stream()
                .map(this::convertirEnDTO)
                .toList();
    }

    @Override
    public CouleurDTO modifierCouleur(Integer id, CouleurUpdateDTO updateDTO) {
        Couleur couleur = trouverCouleur(id);
        String nom = normaliserNom(updateDTO.getNom());

        couleurRepository.findByNomIgnoreCase(nom).ifPresent(couleurExistante -> {
            if (!couleurExistante.getId().equals(id)) {
                throw new CouleurNameAlreadyUsedException();
            }
        });

        couleur.setNom(nom);
        if (updateDTO.getCodeHexadecimal() != null) {
            couleur.setCodeHexadecimal(normaliserCodeHexadecimal(updateDTO.getCodeHexadecimal()));
        }
        if (updateDTO.getActif() != null) {
            couleur.setActif(updateDTO.getActif());
        }
        return convertirEnDTO(couleurRepository.save(couleur));
    }

    @Override
    public void desactiverCouleur(Integer id) {
        Couleur couleur = trouverCouleur(id);
        couleur.setActif(false);
        couleurRepository.save(couleur);
    }

    private Couleur trouverCouleur(Integer id) {
        return couleurRepository.findById(id)
                .orElseThrow(() -> new CouleurNotFoundException(id));
    }

    private void verifierNomDisponible(String nom) {
        if (couleurRepository.existsByNomIgnoreCase(nom)) {
            throw new CouleurNameAlreadyUsedException();
        }
    }

    private String normaliserNom(String nom) {
        if (nom == null) {
            return "";
        }
        return nom.trim();
    }

    private String normaliserCodeHexadecimal(String codeHexadecimal) {
        if (codeHexadecimal == null) {
            throw new CodeHexadecimalInvalideException();
        }
        String code = codeHexadecimal.trim();
        if (!code.matches("^#[0-9A-Fa-f]{6}$")) {
            throw new CodeHexadecimalInvalideException();
        }
        return code.toUpperCase();
    }

    private CouleurDTO convertirEnDTO(Couleur couleur) {
        CouleurDTO dto = new CouleurDTO();
        dto.setId(couleur.getId());
        dto.setNom(couleur.getNom());
        dto.setCodeHexadecimal(couleur.getCodeHexadecimal());
        dto.setActif(couleur.getActif());
        return dto;
    }
}
