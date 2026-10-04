package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.dto.image.ImageProduitDTO;
import com.amansia.amansiabackend.dto.image.ImageProduitUpdateDTO;
import com.amansia.amansiabackend.entity.ImageProduit;
import com.amansia.amansiabackend.entity.Produit;
import com.amansia.amansiabackend.exception.FichierImageInvalideException;
import com.amansia.amansiabackend.exception.ImageProduitNotFoundException;
import com.amansia.amansiabackend.exception.ProduitInactiveException;
import com.amansia.amansiabackend.exception.ProduitNotFoundException;
import com.amansia.amansiabackend.repository.ImageProduitRepository;
import com.amansia.amansiabackend.repository.ProduitRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class ImageProduitServiceImpl implements ImageProduitService {

    private static final long MAX_FILE_SIZE = 5L * 1024L * 1024L;
    private static final String PUBLIC_UPLOAD_PREFIX = "/uploads/produits/";
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final ImageProduitRepository imageProduitRepository;
    private final ProduitRepository produitRepository;
    private final Path uploadDirectory;

    public ImageProduitServiceImpl(
            ImageProduitRepository imageProduitRepository,
            ProduitRepository produitRepository,
            @Value("${app.upload.dir}") String uploadDirectory) {
        this.imageProduitRepository = imageProduitRepository;
        this.produitRepository = produitRepository;
        this.uploadDirectory = Paths.get(uploadDirectory).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDirectory);
        } catch (IOException exception) {
            throw new IllegalStateException("Impossible de creer le dossier de stockage des images", exception);
        }
    }

    @Override
    public ImageProduitDTO upload(MultipartFile file, Integer produitId, Boolean principale, Integer ordre) {
        Produit produit = trouverProduitActif(produitId);
        verifierOrdre(ordre);
        verifierFichier(file);

        Path nouveauFichier = stockerFichier(file);
        ImageProduit image = new ImageProduit();
        image.setProduit(produit);
        image.setUrl(PUBLIC_UPLOAD_PREFIX + nouveauFichier.getFileName());
        image.setPrincipale(Boolean.TRUE.equals(principale));
        image.setOrdre(ordre);

        try {
            imageProduitRepository.save(image);
            if (image.getPrincipale()) {
                mettreCommePrincipale(image);
            }
            return convertirEnDTO(image);
        } catch (RuntimeException exception) {
            if (image.getId() != null) {
                imageProduitRepository.delete(image);
            }
            supprimerFichier(nouveauFichier);
            throw exception;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImageProduitDTO> recupererToutesLesImages() {
        return imageProduitRepository.findAllByOrderByOrdreAsc().stream()
                .map(this::convertirEnDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ImageProduitDTO recupererImageParId(Integer id) {
        return convertirEnDTO(trouverImage(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImageProduitDTO> recupererImagesParProduit(Integer produitId) {
        verifierProduitExiste(produitId);
        return imageProduitRepository.findByProduitIdOrderByOrdreAsc(produitId).stream()
                .map(this::convertirEnDTO)
                .toList();
    }

    @Override
    public ImageProduitDTO modifierImage(Integer id, ImageProduitUpdateDTO updateDTO) {
        ImageProduit image = trouverImage(id);
        verifierOrdre(updateDTO.getOrdre());
        image.setOrdre(updateDTO.getOrdre());
        image.setPrincipale(updateDTO.getPrincipale());
        if (Boolean.TRUE.equals(updateDTO.getPrincipale())) {
            mettreCommePrincipale(image);
        } else {
            imageProduitRepository.save(image);
        }
        return convertirEnDTO(image);
    }

    @Override
    public ImageProduitDTO remplacerFichier(Integer id, MultipartFile file) {
        ImageProduit image = trouverImage(id);
        verifierFichier(file);
        Path ancienFichier = cheminPhysique(image.getUrl());
        Path nouveauFichier = stockerFichier(file);
        String ancienneUrl = image.getUrl();
        image.setUrl(PUBLIC_UPLOAD_PREFIX + nouveauFichier.getFileName());

        try {
            imageProduitRepository.saveAndFlush(image);
            supprimerFichier(ancienFichier);
            return convertirEnDTO(image);
        } catch (RuntimeException exception) {
            image.setUrl(ancienneUrl);
            supprimerFichier(nouveauFichier);
            throw exception;
        }
    }

    @Override
    public ImageProduitDTO definirCommePrincipale(Integer id) {
        ImageProduit image = trouverImage(id);
        mettreCommePrincipale(image);
        return convertirEnDTO(image);
    }

    @Override
    public void supprimerImage(Integer id) {
        ImageProduit image = trouverImage(id);
        boolean etaitPrincipale = Boolean.TRUE.equals(image.getPrincipale());
        Integer produitId = image.getProduit().getId();
        Path fichier = cheminPhysique(image.getUrl());

        supprimerFichier(fichier);
        imageProduitRepository.delete(image);
        imageProduitRepository.flush();

        if (etaitPrincipale) {
            imageProduitRepository.findByProduitIdOrderByOrdreAsc(produitId).stream()
                    .findFirst()
                    .ifPresent(this::mettreCommePrincipale);
        }
    }

    private void mettreCommePrincipale(ImageProduit image) {
        List<ImageProduit> images = imageProduitRepository.findByProduitId(image.getProduit().getId());
        images.forEach(element -> element.setPrincipale(element.getId().equals(image.getId())));
        imageProduitRepository.saveAll(images);
    }

    private Produit trouverProduitActif(Integer produitId) {
        Produit produit = verifierProduitExiste(produitId);
        if (!Boolean.TRUE.equals(produit.getActif())) {
            throw new ProduitInactiveException(produitId);
        }
        return produit;
    }

    private Produit verifierProduitExiste(Integer produitId) {
        return produitRepository.findById(produitId)
                .orElseThrow(() -> new ProduitNotFoundException(produitId));
    }

    private ImageProduit trouverImage(Integer id) {
        return imageProduitRepository.findById(id)
                .orElseThrow(() -> new ImageProduitNotFoundException(id));
    }

    private void verifierOrdre(Integer ordre) {
        if (ordre == null || ordre < 0) {
            throw new FichierImageInvalideException("L'ordre de l'image doit etre superieur ou egal a 0");
        }
    }

    private void verifierFichier(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FichierImageInvalideException("Le fichier image est obligatoire");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FichierImageInvalideException("La taille maximale d'une image est de 5 MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new FichierImageInvalideException("Le type de fichier doit etre JPEG, PNG ou WEBP");
        }

        String extension = extension(file.getOriginalFilename());
        if (!extensionCompatible(contentType, extension)) {
            throw new FichierImageInvalideException("L'extension du fichier ne correspond pas a son type");
        }
    }

    private Path stockerFichier(MultipartFile file) {
        String extension = extension(file.getOriginalFilename());
        Path destination = uploadDirectory.resolve(UUID.randomUUID() + "." + extension).normalize();
        if (!destination.startsWith(uploadDirectory)) {
            throw new FichierImageInvalideException("Nom de fichier invalide");
        }
        try {
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            return destination;
        } catch (IOException exception) {
            throw new FichierImageInvalideException("Impossible de sauvegarder le fichier image");
        }
    }

    private String extension(String originalFilename) {
        String extension = StringUtils.getFilenameExtension(StringUtils.cleanPath(
                originalFilename == null ? "" : originalFilename));
        if (extension == null || extension.isBlank()) {
            throw new FichierImageInvalideException("Le fichier doit avoir une extension image");
        }
        return extension.toLowerCase(Locale.ROOT);
    }

    private boolean extensionCompatible(String contentType, String extension) {
        return ("image/jpeg".equalsIgnoreCase(contentType) && Set.of("jpg", "jpeg").contains(extension))
                || ("image/png".equalsIgnoreCase(contentType) && "png".equals(extension))
                || ("image/webp".equalsIgnoreCase(contentType) && "webp".equals(extension));
    }

    private Path cheminPhysique(String url) {
        if (url == null || !url.startsWith(PUBLIC_UPLOAD_PREFIX)) {
            throw new FichierImageInvalideException("Chemin d'image invalide");
        }
        Path path = uploadDirectory.resolve(url.substring(PUBLIC_UPLOAD_PREFIX.length())).normalize();
        if (!path.startsWith(uploadDirectory)) {
            throw new FichierImageInvalideException("Chemin d'image invalide");
        }
        return path;
    }

    private void supprimerFichier(Path fichier) {
        try {
            Files.deleteIfExists(fichier);
        } catch (IOException exception) {
            throw new FichierImageInvalideException("Impossible de supprimer le fichier image");
        }
    }

    private ImageProduitDTO convertirEnDTO(ImageProduit image) {
        ImageProduitDTO dto = new ImageProduitDTO();
        dto.setId(image.getId());
        dto.setProduitId(image.getProduit().getId());
        dto.setUrl(image.getUrl());
        dto.setPrincipale(image.getPrincipale());
        dto.setOrdre(image.getOrdre());
        return dto;
    }
}
