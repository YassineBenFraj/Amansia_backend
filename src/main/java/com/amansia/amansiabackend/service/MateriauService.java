package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.materiau.MateriauCreationDTO;
import com.amansia.amansiabackend.dto.materiau.MateriauDTO;
import com.amansia.amansiabackend.dto.materiau.MateriauUpdateDTO;

import java.util.List;

public interface MateriauService {

    MateriauDTO creerMateriau(MateriauCreationDTO creationDTO);

    MateriauDTO recupererMateriauParId(Integer id);

    List<MateriauDTO> recupererTousLesMateriaux();

    List<MateriauDTO> recupererMateriauxActifs();

    List<MateriauDTO> rechercherParNom(String nom);

    MateriauDTO modifierMateriau(Integer id, MateriauUpdateDTO updateDTO);

    void desactiverMateriau(Integer id);
}
