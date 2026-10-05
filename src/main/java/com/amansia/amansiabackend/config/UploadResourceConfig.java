package com.amansia.amansiabackend.config;

import com.amansia.amansiabackend.service.CataloguePublicService;
import com.amansia.amansiabackend.repository.UtilisateurRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UploadResourceConfig {
    private final Path directory;
    private final CataloguePublicService catalogue;
    private final UtilisateurRepository utilisateurs;
    @Value("${app.upload.categories-dir:uploads/categories}")
    private String categoriesDirectory;
    @Value("${app.upload.profiles-dir:uploads/profils}")
    private String profilesDirectory;

    public UploadResourceConfig(@Value("${app.upload.dir}") String directory, CataloguePublicService catalogue, UtilisateurRepository utilisateurs) {
        this.directory = Paths.get(directory).toAbsolutePath().normalize();
        this.catalogue = catalogue;
        this.utilisateurs = utilisateurs;
    }

    // La route historique est conservee, mais chaque fichier passe par le controle de visibilite.
    @GetMapping("/uploads/produits/{filename:.+}")
    public ResponseEntity<Resource> image(@PathVariable String filename) throws IOException {
        Path file = directory.resolve(filename).normalize();
        if (!file.startsWith(directory) || !catalogue.fichierVisible("/uploads/produits/" + filename)
                || !Files.isRegularFile(file)) {
            return ResponseEntity.notFound().build();
        }
        if (!file.toRealPath().startsWith(directory.toRealPath())) {
            return ResponseEntity.notFound().build();
        }
        String type = Files.probeContentType(file);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .contentType(type == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(type))
                .body(new FileSystemResource(file));
    }

    @GetMapping("/uploads/categories/{filename:.+}")
    public ResponseEntity<Resource> categoryImage(@PathVariable String filename) throws IOException {
        Path categoryDirectory = Paths.get(categoriesDirectory).toAbsolutePath().normalize();
        Path file = categoryDirectory.resolve(filename).normalize();
        if (!file.startsWith(categoryDirectory) || !Files.isRegularFile(file)
                || !file.toRealPath().startsWith(categoryDirectory.toRealPath())) {
            return ResponseEntity.notFound().build();
        }
        String type = Files.probeContentType(file);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .contentType(type == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(type))
                .body(new FileSystemResource(file));
    }

    @GetMapping("/uploads/profils/{filename:.+}")
    public ResponseEntity<Resource> profileImage(@PathVariable String filename) throws IOException {
        if (!filename.matches("[0-9a-fA-F-]{36}\\.(jpg|png|webp)")
                || !utilisateurs.existsByPhotoProfil("/uploads/profils/" + filename)) return ResponseEntity.notFound().build();
        Path profileDirectory = Paths.get(profilesDirectory).toAbsolutePath().normalize();
        Path file = profileDirectory.resolve(filename).normalize();
        if (!file.startsWith(profileDirectory) || !Files.isRegularFile(file)
                || !file.toRealPath().startsWith(profileDirectory.toRealPath())) return ResponseEntity.notFound().build();
        String type = Files.probeContentType(file);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .contentType(type == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(type))
                .body(new FileSystemResource(file));
    }
}
