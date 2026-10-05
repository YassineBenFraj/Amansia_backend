package com.amansia.amansiabackend.repository;

import com.amansia.amansiabackend.entity.Favori;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FavoriRepository extends JpaRepository<Favori, Integer> {
    @EntityGraph(attributePaths = {"produit", "produit.categorie"})
    List<Favori> findByUtilisateurIdOrderByDateCreationDesc(Integer utilisateurId);

    @EntityGraph(attributePaths = {"produit", "produit.categorie"})
    Optional<Favori> findByUtilisateurIdAndProduitId(Integer utilisateurId, Integer produitId);

    boolean existsByUtilisateurIdAndProduitId(Integer utilisateurId, Integer produitId);

    @Query("select f.utilisateur.id from Favori f where f.produit.id = :productId and f.utilisateur.actif = true")
    List<Integer> activeUserIdsForProduct(@Param("productId") Integer productId);
}
