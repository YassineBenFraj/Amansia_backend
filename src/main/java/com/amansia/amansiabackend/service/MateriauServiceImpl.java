package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.materiau.MateriauCreationDTO;
import com.amansia.amansiabackend.dto.materiau.MateriauDTO;
import com.amansia.amansiabackend.dto.materiau.MateriauUpdateDTO;
import com.amansia.amansiabackend.entity.Materiau;
import com.amansia.amansiabackend.exception.MateriauNameAlreadyUsedException;
import com.amansia.amansiabackend.exception.MateriauNotFoundException;
import com.amansia.amansiabackend.repository.MateriauRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MateriauServiceImpl implements MateriauService {

    private final MateriauRepository materiauRepository;

    @Override
    public MateriauDTO creerMateriau(MateriauCreationDTO creationDTO) {
        String nom = normaliserNom(creationDTO.getNom());
        verifierNomDisponible(nom);

        Materiau materiau = new Materiau();
        materiau.setNom(nom);
        materiau.setDescription(creationDTO.getDescription());
        materiau.setActif(creationDTO.getActif() != null ? creationDTO.getActif() : true);
        return convertirEnDTO(materiauRepository.save(materiau));
    }

    @Override
    @Transactional(readOnly = true)
    public MateriauDTO recupererMateriauParId(Integer id) {
        return convertirEnDTO(trouverMateriau(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MateriauDTO> recupererTousLesMateriaux() {
        return materiauRepository.findAll().stream()
                .map(this::convertirEnDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MateriauDTO> recupererMateriauxActifs() {
        return materiauRepository.findByActifTrue().stream()
                .map(this::convertirEnDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MateriauDTO> rechercherParNom(String nom) {
        String nomRecherche = normaliserNom(nom);
        return materiauRepository.findByNomContainingIgnoreCase(nomRecherche).stream()
                .map(this::convertirEnDTO)
                .toList();
    }

    @Override
    public MateriauDTO modifierMateriau(Integer id, MateriauUpdateDTO updateDTO) {
        Materiau materiau = trouverMateriau(id);
        String nom = normaliserNom(updateDTO.getNom());

        materiauRepository.findByNomIgnoreCase(nom).ifPresent(materiauExistant -> {
            if (!materiauExistant.getId().equals(id)) {
                throw new MateriauNameAlreadyUsedException();
            }
        });

        materiau.setNom(nom);
        materiau.setDescription(updateDTO.getDescription());
        if (updateDTO.getActif() != null) {
            materiau.setActif(updateDTO.getActif());
        }
        return convertirEnDTO(materiauRepository.save(materiau));
    }

    @Override
    public void desactiverMateriau(Integer id) {
        Materiau materiau = trouverMateriau(id);
        materiau.setActif(false);
        materiauRepository.save(materiau);
    }

    private Materiau trouverMateriau(Integer id) {
        return materiauRepository.findById(id)
                .orElseThrow(() -> new MateriauNotFoundException(id));
    }

    private void verifierNomDisponible(String nom) {
        if (materiauRepository.existsByNomIgnoreCase(nom)) {
            throw new MateriauNameAlreadyUsedException();
        }
    }

    private String normaliserNom(String nom) {
        if (nom == null) {
            return "";
        }
        return nom.trim();
    }

    private MateriauDTO convertirEnDTO(Materiau materiau) {
        MateriauDTO dto = new MateriauDTO();
        dto.setId(materiau.getId());
        dto.setNom(materiau.getNom());
        dto.setDescription(materiau.getDescription());
        dto.setActif(materiau.getActif());
        return dto;
    }
}
