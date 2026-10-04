package com.amansia.amansiabackend.repository;

import com.amansia.amansiabackend.entity.VarianteProduit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VarianteProduitRepository extends JpaRepository<VarianteProduit, Integer> {

    List<VarianteProduit> findByProduitId(Integer produitId);

    List<VarianteProduit> findByProduitIdAndActifTrue(Integer produitId);

    Optional<VarianteProduit> findByReferenceIgnoreCase(String reference);

    boolean existsByReferenceIgnoreCase(String reference);

    List<VarianteProduit> findByReferenceContainingIgnoreCase(String reference);
}
