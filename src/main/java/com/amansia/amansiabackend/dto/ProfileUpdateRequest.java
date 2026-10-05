package com.amansia.amansiabackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @NotBlank @Size(max = 100) String nom,
        @NotBlank @Size(max = 100) String prenom,
        @Pattern(regexp = "^$|^[+0-9][0-9 ()-]{6,19}$", message = "Le téléphone est invalide")
        @Size(max = 30) String telephone) {}
