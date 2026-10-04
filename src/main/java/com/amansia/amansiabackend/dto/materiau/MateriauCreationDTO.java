package com.amansia.amansiabackend.dto.materiau;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MateriauCreationDTO {

    @NotBlank
    @Size(max = 100)
    private String nom;

    private String description;

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

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }
}
