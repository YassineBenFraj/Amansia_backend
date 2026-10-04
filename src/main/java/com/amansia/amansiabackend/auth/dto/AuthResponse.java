package com.amansia.amansiabackend.auth.dto;

import com.amansia.amansiabackend.dto.UtilisateurDTO;

public record AuthResponse(String token, String type, UtilisateurDTO utilisateur) {
}
