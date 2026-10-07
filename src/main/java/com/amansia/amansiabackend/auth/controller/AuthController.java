package com.amansia.amansiabackend.auth.controller;

import com.amansia.amansiabackend.auth.dto.AuthResponse;
import com.amansia.amansiabackend.auth.dto.LoginRequest;
import com.amansia.amansiabackend.auth.dto.RegisterRequest;
import com.amansia.amansiabackend.auth.dto.ForgotPasswordRequest;
import com.amansia.amansiabackend.auth.dto.ResetPasswordRequest;
import com.amansia.amansiabackend.auth.service.AuthService;
import com.amansia.amansiabackend.auth.service.PasswordResetService;
import com.amansia.amansiabackend.dto.UtilisateurDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.mail.MailException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UtilisateurDTO> me(Authentication authentication) {
        return ResponseEntity.ok(authService.me(authentication.getName()));
    }

    @PostMapping("/mot-de-passe-oublie")
    public ResponseEntity<Map<String, String>> motDePasseOublie(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            passwordResetService.demander(request.email());
        } catch (MailException exception) {
            // The response never reveals whether the email belongs to an account.
        }
        return ResponseEntity.ok(Map.of("message", PasswordResetService.GENERIC_MESSAGE));
    }

    @PostMapping("/reinitialiser-mot-de-passe")
    public ResponseEntity<Map<String, String>> reinitialiser(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.reinitialiser(request);
        return ResponseEntity.ok(Map.of("message", "Votre mot de passe a été réinitialisé avec succès."));
    }
}
