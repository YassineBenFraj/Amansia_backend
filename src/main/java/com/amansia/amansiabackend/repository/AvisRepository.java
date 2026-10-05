package com.amansia.amansiabackend.repository;

import com.amansia.amansiabackend.entity.Avis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvisRepository extends JpaRepository<Avis, Integer> {

    List<Avis> findByProduitIdAndApprouveTrueOrderByDateCreationDesc(Integer produitId);

    @Query("select a from Avis a where a.produit.id = :produitId and (a.approuve = true or a.utilisateur.id = :utilisateurId) order by a.dateCreation desc")
    List<Avis> findPublicAndOwnedByProduct(@Param("produitId") Integer produitId, @Param("utilisateurId") Integer utilisateurId);

    List<Avis> findByProduitIdOrderByDateCreationDesc(Integer produitId);

    List<Avis> findAllByOrderByDateCreationDesc();

    Optional<Avis> findByIdAndApprouveTrue(Integer id);

    boolean existsByUtilisateurIdAndProduitIdAndCommandeId(Integer utilisateurId, Integer produitId, Integer commandeId);
}
