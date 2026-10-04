package com.amansia.amansiabackend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "couleurs")
public class Couleur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nom", nullable = false, unique = true, length = 100)
    private String nom;

    @Column(name = "code_hexadecimal", nullable = true, length = 7)
    private String codeHexadecimal;

    @Column(name = "actif", nullable = false)
    private Boolean actif;

    @PrePersist
    protected void onCreate() {
        if (actif == null) {
            actif = true;
        }
    }

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
