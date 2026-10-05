package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.config.UploadResourceConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImageResourceTest {
    @TempDir Path directory;

    @Test
    void inactiveImageFileIsNotServedEvenIfItExists() throws Exception {
        Files.writeString(directory.resolve("a.png"), "image");
        var catalogue = mock(CataloguePublicService.class);
        var controller = new UploadResourceConfig(directory.toString(), catalogue, mock(com.amansia.amansiabackend.repository.UtilisateurRepository.class));
        assertEquals(404, controller.image("a.png").getStatusCode().value());
    }

    @Test
    void activeImageFileIsServedWithoutPersistentCache() throws Exception {
        Files.writeString(directory.resolve("a.png"), "image");
        var catalogue = mock(CataloguePublicService.class);
        when(catalogue.fichierVisible("/uploads/produits/a.png")).thenReturn(true);
        var controller = new UploadResourceConfig(directory.toString(), catalogue, mock(com.amansia.amansiabackend.repository.UtilisateurRepository.class));
        var response = controller.image("a.png");
        assertEquals(200, response.getStatusCode().value());
        assertEquals("no-store", response.getHeaders().getCacheControl());
        assertEquals(404, controller.image("../a.png").getStatusCode().value());
    }
}
