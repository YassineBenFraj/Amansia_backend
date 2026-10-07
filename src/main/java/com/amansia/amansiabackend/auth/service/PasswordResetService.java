package com.amansia.amansiabackend.auth.service;

import com.amansia.amansiabackend.auth.dto.ResetPasswordRequest;
import com.amansia.amansiabackend.entity.JetonReinitialisationMdp;
import com.amansia.amansiabackend.repository.JetonReinitialisationMdpRepository;
import com.amansia.amansiabackend.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class PasswordResetService {
    public static final String GENERIC_MESSAGE =
            "Si un compte existe avec cette adresse, un lien de réinitialisation vous sera envoyé.";
    private static final SecureRandom RANDOM = new SecureRandom();
    private final UtilisateurRepository utilisateurs;
    private final JetonReinitialisationMdpRepository jetons;
    private final PasswordEncoder passwords;
    private final EmailService email;
    private final long expirationMinutes;
    private final long cooldownMinutes;

    public PasswordResetService(UtilisateurRepository utilisateurs, JetonReinitialisationMdpRepository jetons,
            PasswordEncoder passwords, EmailService email,
            @Value("${app.password-reset.expiration-minutes:30}") long expirationMinutes,
            @Value("${app.password-reset.cooldown-minutes:2}") long cooldownMinutes) {
        if (expirationMinutes < 1 || cooldownMinutes < 1)
            throw new IllegalArgumentException("La durée des jetons et le délai entre demandes doivent être positifs.");
        this.utilisateurs = utilisateurs;
        this.jetons = jetons;
        this.passwords = passwords;
        this.email = email;
        this.expirationMinutes = expirationMinutes;
        this.cooldownMinutes = cooldownMinutes;
    }

    @Transactional
    public String demander(String adresse) {
        email.verifierConfiguration(); // Identical configuration response for known and unknown accounts.
        String normalisee = adresse.trim().toLowerCase(Locale.ROOT);
        var utilisateur = utilisateurs.findByEmailForUpdate(normalisee).orElse(null);
        if (utilisateur == null || !Boolean.TRUE.equals(utilisateur.getActif())) return GENERIC_MESSAGE;

        LocalDateTime maintenant = LocalDateTime.now();
        var dernier = jetons.findTopByUtilisateurIdOrderByDateCreationDesc(utilisateur.getId());
        if (dernier.isPresent() && dernier.get().getDateCreation().isAfter(maintenant.minusMinutes(cooldownMinutes)))
            return GENERIC_MESSAGE;

        jetons.invaliderJetonsActifs(utilisateur.getId());
        byte[] aleatoire = new byte[32];
        RANDOM.nextBytes(aleatoire);
        String brut = Base64.getUrlEncoder().withoutPadding().encodeToString(aleatoire);
        JetonReinitialisationMdp jeton = new JetonReinitialisationMdp();
        jeton.setUtilisateur(utilisateur);
        jeton.setTokenHash(hash(brut));
        jeton.setDateCreation(maintenant);
        jeton.setDateExpiration(maintenant.plusMinutes(expirationMinutes));
        jetons.save(jeton);
        // A mail failure rolls back the token and the invalidation in InnoDB.
        email.envoyerReinitialisation(utilisateur.getEmail(), brut, expirationMinutes);
        return GENERIC_MESSAGE;
    }

    @Transactional
    public void reinitialiser(ResetPasswordRequest request) {
        if (!request.nouveauMotDePasse().equals(request.confirmationMotDePasse()))
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas.");
        if (request.nouveauMotDePasse().length() < 8)
            throw new IllegalArgumentException("Le nouveau mot de passe doit contenir au moins 8 caractères.");
        if (request.token() == null || !request.token().matches("[A-Za-z0-9_-]{43}"))
            throw new LienReinitialisationException("Ce lien de réinitialisation est invalide.");

        String empreinte = hash(request.token());
        Integer utilisateurId = jetons.findUtilisateurIdByTokenHash(empreinte)
                .orElseThrow(() -> new LienReinitialisationException("Ce lien de réinitialisation est invalide."));
        // Lock order is user then token, as in demander(), to serialize concurrent requests.
        var utilisateur = utilisateurs.findByIdForUpdate(utilisateurId)
                .orElseThrow(() -> new LienReinitialisationException("Ce lien de réinitialisation est invalide."));
        var jeton = jetons.findByTokenHashForUpdate(empreinte)
                .orElseThrow(() -> new LienReinitialisationException("Ce lien de réinitialisation est invalide."));
        if (jeton.isUtilise())
            throw new LienReinitialisationException("Ce lien de réinitialisation a déjà été utilisé ou remplacé.");
        if (!LocalDateTime.now().isBefore(jeton.getDateExpiration()))
            throw new LienReinitialisationException("Ce lien de réinitialisation a expiré.");
        if (!Boolean.TRUE.equals(utilisateur.getActif()))
            throw new LienReinitialisationException("Ce lien de réinitialisation est invalide.");

        utilisateur.setMotDePasse(passwords.encode(request.nouveauMotDePasse()));
        jeton.setUtilise(true);
        utilisateurs.save(utilisateur);
        jetons.save(jeton);
    }

    private static String hash(String brut) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(brut.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }
}
