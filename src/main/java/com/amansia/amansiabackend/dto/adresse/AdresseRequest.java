package com.amansia.amansiabackend.dto.adresse;
import jakarta.validation.constraints.*;
public record AdresseRequest(@NotBlank @Size(max=255) String adresse,
 @NotBlank @Size(max=100) String ville, @Size(max=100) String gouvernorat,
 @Size(max=20) String codePostal, @Size(max=100) String pays, Boolean adressePrincipale) {}
