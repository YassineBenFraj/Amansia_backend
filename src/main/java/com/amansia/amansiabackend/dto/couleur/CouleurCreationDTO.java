package com.amansia.amansiabackend.dto.couleur;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CouleurCreationDTO {

    @NotBlank
    @Size(max = 100)
    private String nom;

    @NotBlank
    private String codeHexadecimal;

    private Boolean actif;

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getCodeHexadecimal() {
        return codeHexadecimal;
    }

    public void setCodeHexadecimal(String codeHexadecimal) {
        this.codeHexadecimal = codeHexadecimal;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }
}
