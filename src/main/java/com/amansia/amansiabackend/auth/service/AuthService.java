package com.amansia.amansiabackend.auth.service;

import com.amansia.amansiabackend.auth.dto.AuthResponse;
import com.amansia.amansiabackend.auth.dto.LoginRequest;
import com.amansia.amansiabackend.auth.dto.RegisterRequest;
import com.amansia.amansiabackend.dto.UtilisateurDTO;
import com.amansia.amansiabackend.entity.Role;
import com.amansia.amansiabackend.entity.Utilisateur;
import com.amansia.amansiabackend.exception.EmailAlreadyUsedException;
import com.amansia.amansiabackend.exception.UtilisateurNotFoundException;
import com.amansia.amansiabackend.repository.UtilisateurRepository;
import com.amansia.amansiabackend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (utilisateurRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyUsedException();
        }
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(request.nom());
        utilisateur.setPrenom(request.prenom());
        utilisateur.setEmail(request.email());
        utilisateur.setMotDePasse(passwordEncoder.encode(request.motDePasse()));
        utilisateur.setTelephone(request.telephone());
        utilisateur.setDateNaissance(request.dateNaissance());
        utilisateur.setRole(Role.CLIENT);
        utilisateur.setActif(true);
        utilisateur = utilisateurRepository.save(utilisateur);
        return response(utilisateur);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.motDePasse()));
        Utilisateur utilisateur = utilisateurRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UtilisateurNotFoundException("email", request.email()));
        return response(utilisateur);
    }

    public UtilisateurDTO me(String email) {
        return utilisateurRepository.findByEmail(email)
                .map(this::toDto)
                .orElseThrow(() -> new UtilisateurNotFoundException("email", email));
    }

    private AuthResponse response(Utilisateur utilisateur) {
        return new AuthResponse(jwtUtil.generateToken(utilisateur.getEmail(), utilisateur.getRole().name()),
                "Bearer", toDto(utilisateur));
    }

    private UtilisateurDTO toDto(Utilisateur utilisateur) {
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setId(utilisateur.getId());
        dto.setNom(utilisateur.getNom());
        dto.setPrenom(utilisateur.getPrenom());
        dto.setEmail(utilisateur.getEmail());
        dto.setTelephone(utilisateur.getTelephone());
        dto.setDateNaissance(utilisateur.getDateNaissance());
        dto.setRole(utilisateur.getRole());
        dto.setActif(utilisateur.getActif());
        dto.setDateCreation(utilisateur.getDateCreation());
        dto.setDateModification(utilisateur.getDateModification());
        return dto;
    }
}
