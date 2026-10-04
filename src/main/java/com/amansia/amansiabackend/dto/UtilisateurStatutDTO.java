package com.amansia.amansiabackend.dto;

import jakarta.validation.constraints.NotNull;

public record UtilisateurStatutDTO(@NotNull Boolean actif) {
}
