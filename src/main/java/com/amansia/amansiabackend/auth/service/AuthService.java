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
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final GoogleCredentialVerifier googleCredentials;
    private static final SecureRandom RANDOM = new SecureRandom();

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

    @Transactional
    public AuthResponse google(String credential) {
        var identity = googleCredentials.verify(credential);
        if (!identity.emailVerified() || identity.subject() == null || identity.subject().isBlank()
                || identity.email() == null || identity.email().isBlank())
            throw new IllegalArgumentException("Connexion Google invalide.");

        String email = identity.email().trim().toLowerCase(Locale.ROOT);
        Utilisateur linked = utilisateurRepository.findByGoogleSub(identity.subject()).orElse(null);
        if (linked != null) {
            if (!Boolean.TRUE.equals(linked.getActif())) throw new IllegalArgumentException("Connexion impossible.");
            return response(linked);
        }

        Utilisateur utilisateur = utilisateurRepository.findByEmailForUpdate(email).orElse(null);
        if (utilisateur != null) {
            if (!Boolean.TRUE.equals(utilisateur.getActif()) || utilisateur.getGoogleSub() != null)
                throw new IllegalArgumentException("Connexion impossible.");
            // A verified email alone is not proof of continuing ownership for third-party domains.
            String domain = email.substring(email.lastIndexOf('@') + 1);
            boolean authoritative = domain.equals("gmail.com")
                    || (identity.hostedDomain() != null && domain.equalsIgnoreCase(identity.hostedDomain()));
            if (!authoritative) throw new IllegalArgumentException(
                    "Connectez-vous avec votre mot de passe pour ce compte.");
            utilisateur.setGoogleSub(identity.subject());
            utilisateurRepository.save(utilisateur);
            return response(utilisateur);
        }

        utilisateur = new Utilisateur();
        utilisateur.setEmail(email);
        utilisateur.setGoogleSub(identity.subject());
        utilisateur.setPrenom(nonBlank(identity.givenName(), "Client"));
        utilisateur.setNom(nonBlank(identity.familyName(), "Amansia"));
        byte[] random = new byte[48];
        RANDOM.nextBytes(random);
        utilisateur.setMotDePasse(passwordEncoder.encode(Base64.getUrlEncoder().withoutPadding().encodeToString(random)));
        utilisateur.setRole(Role.CLIENT);
        utilisateur.setActif(true);
        return response(utilisateurRepository.saveAndFlush(utilisateur));
    }

    @Transactional
    public UtilisateurDTO associerGoogle(String emailCompte, String credential) {
        var identity = googleCredentials.verify(credential);
        if (!identity.emailVerified() || identity.subject() == null || identity.subject().isBlank()
                || identity.email() == null || !identity.email().trim().equalsIgnoreCase(emailCompte))
            throw new IllegalArgumentException("Le compte Google doit utiliser la même adresse email.");
        Utilisateur utilisateur = utilisateurRepository.findByEmailForUpdate(emailCompte)
                .orElseThrow(() -> new UtilisateurNotFoundException("email", emailCompte));
        if (!Boolean.TRUE.equals(utilisateur.getActif())) throw new IllegalArgumentException("Connexion impossible.");
        if (utilisateur.getGoogleSub() != null && !utilisateur.getGoogleSub().equals(identity.subject()))
            throw new IllegalArgumentException("Un autre compte Google est déjà associé.");
        var owner = utilisateurRepository.findByGoogleSub(identity.subject());
        if (owner.isPresent() && !owner.get().getId().equals(utilisateur.getId()))
            throw new IllegalArgumentException("Ce compte Google est déjà associé.");
        if (utilisateur.getGoogleSub() == null) {
            utilisateur.setGoogleSub(identity.subject());
            utilisateurRepository.save(utilisateur);
        }
        return toDto(utilisateur);
    }

    private static String nonBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
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
        dto.setPhotoProfil(utilisateur.getPhotoProfil());
        dto.setDateNaissance(utilisateur.getDateNaissance());
        dto.setRole(utilisateur.getRole());
        dto.setActif(utilisateur.getActif());
        dto.setDateCreation(utilisateur.getDateCreation());
        dto.setDateModification(utilisateur.getDateModification());
        return dto;
    }
}
