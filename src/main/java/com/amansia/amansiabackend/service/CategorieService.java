package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.categorie.CategorieCreationDTO;
import com.amansia.amansiabackend.dto.categorie.CategorieDTO;
import com.amansia.amansiabackend.dto.categorie.CategorieUpdateDTO;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface CategorieService {

    CategorieDTO creerCategorie(CategorieCreationDTO creationDTO);

    CategorieDTO recupererCategorieParId(Integer id);

    List<CategorieDTO> recupererToutesLesCategories();

    List<CategorieDTO> recupererCategoriesActives();

    CategorieDTO modifierCategorie(Integer id, CategorieUpdateDTO updateDTO);

    void desactiverCategorie(Integer id);

    CategorieDTO uploaderImage(Integer id, MultipartFile file);
}
