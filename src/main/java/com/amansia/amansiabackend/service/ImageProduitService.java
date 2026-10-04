package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.image.ImageProduitDTO;
import com.amansia.amansiabackend.dto.image.ImageProduitUpdateDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageProduitService {

    ImageProduitDTO upload(MultipartFile file, Integer produitId, Boolean principale, Integer ordre);

    List<ImageProduitDTO> recupererToutesLesImages();

    ImageProduitDTO recupererImageParId(Integer id);

    List<ImageProduitDTO> recupererImagesParProduit(Integer produitId);

    ImageProduitDTO modifierImage(Integer id, ImageProduitUpdateDTO updateDTO);

    ImageProduitDTO remplacerFichier(Integer id, MultipartFile file);

    ImageProduitDTO definirCommePrincipale(Integer id);

    void supprimerImage(Integer id);
}
