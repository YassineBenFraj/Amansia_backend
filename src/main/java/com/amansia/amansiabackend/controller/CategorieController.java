package com.amansia.amansiabackend.controller;

import com.amansia.amansiabackend.dto.categorie.CategorieCreationDTO;
import com.amansia.amansiabackend.dto.categorie.CategorieDTO;
import com.amansia.amansiabackend.dto.categorie.CategorieUpdateDTO;
import com.amansia.amansiabackend.service.CategorieService;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategorieController {

    private final CategorieService categorieService;

    @PostMapping
    public ResponseEntity<CategorieDTO> creerCategorie(@Valid @RequestBody CategorieCreationDTO creationDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categorieService.creerCategorie(creationDTO));
    }

    @GetMapping
    public ResponseEntity<List<CategorieDTO>> recupererToutesLesCategories() {
        return ResponseEntity.ok(categorieService.recupererToutesLesCategories());
    }

    @GetMapping("/actives")
    public ResponseEntity<List<CategorieDTO>> recupererCategoriesActives() {
        return ResponseEntity.ok(categorieService.recupererCategoriesActives());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategorieDTO> recupererCategorieParId(@PathVariable Integer id) {
        return ResponseEntity.ok(categorieService.recupererCategorieParId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategorieDTO> modifierCategorie(
            @PathVariable Integer id,
            @Valid @RequestBody CategorieUpdateDTO updateDTO) {
        return ResponseEntity.ok(categorieService.modifierCategorie(id, updateDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactiverCategorie(@PathVariable Integer id) {
        categorieService.desactiverCategorie(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<CategorieDTO> uploaderImage(@PathVariable Integer id, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(categorieService.uploaderImage(id, file));
    }
}
