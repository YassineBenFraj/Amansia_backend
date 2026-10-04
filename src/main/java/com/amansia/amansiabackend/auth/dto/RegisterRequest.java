package com.amansia.amansiabackend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterRequest(
        @NotBlank String nom,
        @NotBlank String prenom,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String motDePasse,
        @Pattern(regexp = "^$|^[+0-9][0-9 ()-]{6,19}$", message = "Le téléphone est invalide") String telephone,
        LocalDate dateNaissance) {
}
