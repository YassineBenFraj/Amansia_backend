package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.categorie.CategorieCreationDTO;
import com.amansia.amansiabackend.dto.categorie.CategorieDTO;
import com.amansia.amansiabackend.dto.categorie.CategorieUpdateDTO;
import com.amansia.amansiabackend.entity.Categorie;
import com.amansia.amansiabackend.exception.CategorieNameAlreadyUsedException;
import com.amansia.amansiabackend.exception.CategorieNotFoundException;
import com.amansia.amansiabackend.repository.CategorieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategorieServiceImpl implements CategorieService {

    private final CategorieRepository categorieRepository;

    @org.springframework.beans.factory.annotation.Value("${app.upload.categories-dir:uploads/categories}")
    private String categoriesUploadDirectory;

    @Override
    public CategorieDTO creerCategorie(CategorieCreationDTO creationDTO) {
        String nom = normaliserNom(creationDTO.getNom());
        verifierNomDisponible(nom);

        Categorie categorie = new Categorie();
        categorie.setNom(nom);
        categorie.setDescription(creationDTO.getDescription());
        categorie.setImage(creationDTO.getImage());
        categorie.setActif(true);
        return convertirEnDTO(categorieRepository.save(categorie));
    }

    @Override
    public CategorieDTO recupererCategorieParId(Integer id) {
        return convertirEnDTO(trouverCategorie(id));
    }

    @Override
    public List<CategorieDTO> recupererToutesLesCategories() {
        return categorieRepository.findAll().stream()
                .map(this::convertirEnDTO)
                .toList();
    }

    @Override
    public List<CategorieDTO> recupererCategoriesActives() {
        return categorieRepository.findByActifTrue().stream()
                .map(this::convertirEnDTO)
                .toList();
    }

    @Override
    public CategorieDTO modifierCategorie(Integer id, CategorieUpdateDTO updateDTO) {
        Categorie categorie = trouverCategorie(id);
        String nom = normaliserNom(updateDTO.getNom());

        categorieRepository.findByNomIgnoreCase(nom).ifPresent(categorieExistante -> {
            if (!categorieExistante.getId().equals(id)) {
                throw new CategorieNameAlreadyUsedException();
            }
        });

        categorie.setNom(nom);
        categorie.setDescription(updateDTO.getDescription());
        categorie.setImage(updateDTO.getImage());
        if (updateDTO.getActif() != null) {
            categorie.setActif(updateDTO.getActif());
        }
        return convertirEnDTO(categorieRepository.save(categorie));
    }

    @Override
    public void desactiverCategorie(Integer id) {
        Categorie categorie = trouverCategorie(id);
        categorie.setActif(false);
        categorieRepository.save(categorie);
    }

    @Override
    public CategorieDTO uploaderImage(Integer id, MultipartFile file) {
        Categorie categorie = trouverCategorie(id);
        if (file == null || file.isEmpty() || file.getContentType() == null
                || !List.of("image/jpeg", "image/png", "image/webp").contains(file.getContentType().toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Le fichier doit être une image JPEG, PNG ou WEBP");
        }
        if (file.getSize() > 5L * 1024L * 1024L) {
            throw new IllegalArgumentException("La taille maximale de l'image est de 5 MB");
        }
        String originalName = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
        String extension = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT) : "bin";
        Path directory = Paths.get(categoriesUploadDirectory).toAbsolutePath().normalize();
        try {
            Files.createDirectories(directory);
            Path destination = directory.resolve(UUID.randomUUID() + "." + extension).normalize();
            if (!destination.startsWith(directory)) throw new IllegalArgumentException("Nom de fichier invalide");
            file.transferTo(destination);
            categorie.setImage("/uploads/categories/" + destination.getFileName());
            return convertirEnDTO(categorieRepository.save(categorie));
        } catch (IOException exception) {
            throw new IllegalStateException("Impossible de stocker l'image de la catégorie", exception);
        }
    }

    private Categorie trouverCategorie(Integer id) {
        return categorieRepository.findById(id)
                .orElseThrow(() -> new CategorieNotFoundException(id));
    }

    private void verifierNomDisponible(String nom) {
        if (categorieRepository.existsByNomIgnoreCase(nom)) {
            throw new CategorieNameAlreadyUsedException();
        }
    }

    private String normaliserNom(String nom) {
        return nom.trim();
    }

    private CategorieDTO convertirEnDTO(Categorie categorie) {
        CategorieDTO dto = new CategorieDTO();
        dto.setId(categorie.getId());
        dto.setNom(categorie.getNom());
        dto.setDescription(categorie.getDescription());
        dto.setImage(categorie.getImage());
        dto.setActif(categorie.getActif());
        dto.setDateCreation(categorie.getDateCreation());
        dto.setDateModification(categorie.getDateModification());
        return dto;
    }
}
