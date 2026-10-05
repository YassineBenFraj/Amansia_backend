package com.amansia.amansiabackend.controller;

import com.amansia.amansiabackend.dto.PasswordChangeRequest;
import com.amansia.amansiabackend.dto.ProfileUpdateRequest;
import com.amansia.amansiabackend.dto.UtilisateurDTO;
import com.amansia.amansiabackend.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/utilisateurs/me")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profile;

    @PutMapping
    public ResponseEntity<UtilisateurDTO> update(Authentication authentication, @Valid @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(profile.update(authentication.getName(), request));
    }

    @PostMapping("/photo")
    public ResponseEntity<UtilisateurDTO> upload(Authentication authentication, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(profile.upload(authentication.getName(), file));
    }

    @DeleteMapping("/photo")
    public ResponseEntity<UtilisateurDTO> deletePhoto(Authentication authentication) {
        return ResponseEntity.ok(profile.deletePhoto(authentication.getName()));
    }

    @PutMapping("/mot-de-passe")
    public ResponseEntity<Void> password(Authentication authentication, @Valid @RequestBody PasswordChangeRequest request) {
        profile.changePassword(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }
}
