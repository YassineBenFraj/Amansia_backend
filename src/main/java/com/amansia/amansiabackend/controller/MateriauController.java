package com.amansia.amansiabackend.controller;

import com.amansia.amansiabackend.dto.materiau.MateriauCreationDTO;
import com.amansia.amansiabackend.dto.materiau.MateriauDTO;
import com.amansia.amansiabackend.dto.materiau.MateriauUpdateDTO;
import com.amansia.amansiabackend.service.MateriauService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/materiaux")
@RequiredArgsConstructor
public class MateriauController {

    private final MateriauService materiauService;

    @PostMapping
    public ResponseEntity<MateriauDTO> creerMateriau(@Valid @RequestBody MateriauCreationDTO creationDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(materiauService.creerMateriau(creationDTO));
    }

    @GetMapping
    public ResponseEntity<List<MateriauDTO>> recupererTousLesMateriaux() {
        return ResponseEntity.ok(materiauService.recupererTousLesMateriaux());
    }

    @GetMapping("/actifs")
    public ResponseEntity<List<MateriauDTO>> recupererMateriauxActifs() {
        return ResponseEntity.ok(materiauService.recupererMateriauxActifs());
    }

    @GetMapping("/recherche")
    public ResponseEntity<List<MateriauDTO>> rechercherParNom(@RequestParam String nom) {
        return ResponseEntity.ok(materiauService.rechercherParNom(nom));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MateriauDTO> recupererMateriauParId(@PathVariable Integer id) {
        return ResponseEntity.ok(materiauService.recupererMateriauParId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MateriauDTO> modifierMateriau(
            @PathVariable Integer id,
            @Valid @RequestBody MateriauUpdateDTO updateDTO) {
        return ResponseEntity.ok(materiauService.modifierMateriau(id, updateDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactiverMateriau(@PathVariable Integer id) {
        materiauService.desactiverMateriau(id);
        return ResponseEntity.noContent().build();
    }
}
