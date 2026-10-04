package com.amansia.amansiabackend.repository;

import com.amansia.amansiabackend.entity.Favori;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriRepository extends JpaRepository<Favori, Integer> {
    @EntityGraph(attributePaths = {"produit", "produit.categorie"})
    List<Favori> findByUtilisateurIdOrderByDateCreationDesc(Integer utilisateurId);

    @EntityGraph(attributePaths = {"produit", "produit.categorie"})
    Optional<Favori> findByUtilisateurIdAndProduitId(Integer utilisateurId, Integer produitId);

    boolean existsByUtilisateurIdAndProduitId(Integer utilisateurId, Integer produitId);
}
