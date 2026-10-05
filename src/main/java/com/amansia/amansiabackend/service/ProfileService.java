package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.PasswordChangeRequest;
import com.amansia.amansiabackend.dto.ProfileUpdateRequest;
import com.amansia.amansiabackend.dto.UtilisateurDTO;
import com.amansia.amansiabackend.entity.Utilisateur;
import com.amansia.amansiabackend.exception.FichierImageInvalideException;
import com.amansia.amansiabackend.exception.UtilisateurNotFoundException;
import com.amansia.amansiabackend.repository.UtilisateurRepository;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProfileService {
    private static final long MAX_SIZE = 3L * 1024 * 1024;
    private static final String PREFIX = "/uploads/profils/";
    private static final Map<String, String> TYPES = Map.of("image/jpeg", "jpg", "image/png", "png", "image/webp", "webp");
    private final UtilisateurRepository users;
    private final UtilisateurService usersService;
    private final PasswordEncoder passwordEncoder;
    private final Path directory;

    public ProfileService(UtilisateurRepository users, UtilisateurService usersService, PasswordEncoder passwordEncoder,
            @Value("${app.upload.profiles-dir:uploads/profils}") String directory) {
        this.users = users;
        this.usersService = usersService;
        this.passwordEncoder = passwordEncoder;
        this.directory = Paths.get(directory).toAbsolutePath().normalize();
    }

    public UtilisateurDTO update(String email, ProfileUpdateRequest request) {
        Utilisateur user = user(email);
        user.setNom(request.nom().trim());
        user.setPrenom(request.prenom().trim());
        user.setTelephone(request.telephone() == null ? null : request.telephone().trim());
        return dto(users.save(user));
    }

    public void changePassword(String email, PasswordChangeRequest request) {
        Utilisateur user = user(email);
        if (!passwordEncoder.matches(request.motDePasseActuel(), user.getMotDePasse()))
            throw new IllegalArgumentException("Le mot de passe actuel est incorrect.");
        if (!request.nouveauMotDePasse().equals(request.confirmation()))
            throw new IllegalArgumentException("La confirmation ne correspond pas au nouveau mot de passe.");
        if (passwordEncoder.matches(request.nouveauMotDePasse(), user.getMotDePasse()))
            throw new IllegalArgumentException("Le nouveau mot de passe doit être différent de l'ancien.");
        user.setMotDePasse(passwordEncoder.encode(request.nouveauMotDePasse()));
        users.save(user);
    }

    public UtilisateurDTO upload(String email, MultipartFile file) {
        String extension = validate(file);
        Utilisateur user = user(email);
        try {
            Files.createDirectories(directory);
            Path destination = directory.resolve(UUID.randomUUID() + "." + extension);
            try (InputStream stream = file.getInputStream()) {
                Files.copy(stream, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException exception) {
                Files.deleteIfExists(destination);
                throw exception;
            }
            String previous = user.getPhotoProfil();
            user.setPhotoProfil(PREFIX + destination.getFileName());
            try {
                users.saveAndFlush(user);
            } catch (RuntimeException exception) {
                Files.deleteIfExists(destination);
                throw exception;
            }
            deleteUnused(previous);
            return dto(user);
        } catch (IOException exception) {
            throw new IllegalStateException("Impossible de stocker la photo de profil.", exception);
        }
    }

    public UtilisateurDTO deletePhoto(String email) {
        Utilisateur user = user(email);
        String previous = user.getPhotoProfil();
        user.setPhotoProfil(null);
        UtilisateurDTO updated = dto(users.saveAndFlush(user));
        deleteUnused(previous);
        return updated;
    }

    private Utilisateur user(String email) {
        return users.findByEmail(email).orElseThrow(() -> new UtilisateurNotFoundException("email", email));
    }
    private UtilisateurDTO dto(Utilisateur user) { return usersService.recupererUtilisateurParId(user.getId()); }

    private String validate(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() > MAX_SIZE) throw new FichierImageInvalideException("Photo vide ou supérieure à 3 Mo.");
        String type = file.getContentType();
        String extension = TYPES.get(type);
        if (extension == null) throw new FichierImageInvalideException("Format accepté : JPEG, PNG ou WebP.");
        String original = file.getOriginalFilename();
        String suffix = original == null || !original.contains(".") ? "" : original.substring(original.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!(suffix.equals(extension) || extension.equals("jpg") && suffix.equals("jpeg")))
            throw new FichierImageInvalideException("L'extension de la photo ne correspond pas à son format.");
        try (InputStream stream = file.getInputStream()) {
            byte[] header = stream.readNBytes(12);
            boolean valid = switch (type) {
                case "image/jpeg" -> header.length >= 3 && (header[0] & 255) == 255 && (header[1] & 255) == 216 && (header[2] & 255) == 255;
                case "image/png" -> header.length >= 8 && (header[0] & 255) == 137 && header[1] == 80 && header[2] == 78 && header[3] == 71;
                case "image/webp" -> header.length >= 12 && new String(header, 0, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("RIFF") && new String(header, 8, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("WEBP");
                default -> false;
            };
            if (!valid) throw new FichierImageInvalideException("Le fichier ne correspond pas à une image valide.");
        } catch (IOException exception) { throw new FichierImageInvalideException("Impossible de lire la photo."); }
        return extension;
    }

    private void deleteUnused(String url) {
        if (url == null || !url.startsWith(PREFIX)) return;
        String filename = url.substring(PREFIX.length());
        if (!filename.matches("[0-9a-fA-F-]{36}\\.(jpg|png|webp)")) return;
        Path path = directory.resolve(filename).normalize();
        if (!path.startsWith(directory)) return;
        try { if (!users.existsByPhotoProfil(url)) Files.deleteIfExists(path); }
        catch (IOException | RuntimeException ignored) { /* Orphan safe to clean later. */ }
    }
}
