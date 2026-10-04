package com.amansia.amansiabackend.repository;

import com.amansia.amansiabackend.entity.LigneCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LigneCommandeRepository extends JpaRepository<LigneCommande, Integer> {

    List<LigneCommande> findByCommandeId(Integer commandeId);

    boolean existsByCommandeIdAndProduitId(Integer commandeId, Integer produitId);

    void deleteByCommandeId(Integer commandeId);
}
