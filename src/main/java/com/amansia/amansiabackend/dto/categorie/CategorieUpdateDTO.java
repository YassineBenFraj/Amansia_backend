package com.amansia.amansiabackend.dto.categorie;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategorieUpdateDTO {

    @NotBlank
    @Size(max = 150)
    private String nom;

    private String description;

    @Size(max = 500)
    private String image;

    private Boolean actif;

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }
}
