package com.amansia.amansiabackend.dto.adresse;
import java.time.LocalDateTime;
public record AdresseDTO(Integer id,String adresse,String ville,String gouvernorat,String codePostal,String pays,Boolean adressePrincipale,LocalDateTime dateCreation) {}
