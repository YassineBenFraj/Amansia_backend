package com.amansia.amansiabackend.repository;

import com.amansia.amansiabackend.entity.Avis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvisRepository extends JpaRepository<Avis, Integer> {

    List<Avis> findByProduitIdAndApprouveTrueOrderByDateCreationDesc(Integer produitId);

    List<Avis> findByProduitIdOrderByDateCreationDesc(Integer produitId);

    List<Avis> findAllByOrderByDateCreationDesc();

    Optional<Avis> findByIdAndApprouveTrue(Integer id);

    boolean existsByUtilisateurIdAndProduitIdAndCommandeId(Integer utilisateurId, Integer produitId, Integer commandeId);
}
