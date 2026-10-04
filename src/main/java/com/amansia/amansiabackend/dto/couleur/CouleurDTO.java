package com.amansia.amansiabackend.dto.couleur;

import java.time.LocalDateTime;

public class CouleurDTO {

    private Integer id;
    private String nom;
    private String codeHexadecimal;
    private Boolean actif;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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
