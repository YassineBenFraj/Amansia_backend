package com.amansia.amansiabackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequest(
        @NotBlank String motDePasseActuel,
        @NotBlank @Size(min = 8, max = 255) String nouveauMotDePasse,
        @NotBlank String confirmation) {}
