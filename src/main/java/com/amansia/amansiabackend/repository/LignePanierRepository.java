package com.amansia.amansiabackend.repository;

import com.amansia.amansiabackend.entity.LignePanier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LignePanierRepository extends JpaRepository<LignePanier, Integer> {

    List<LignePanier> findByPanierId(Integer panierId);

    Optional<LignePanier> findByIdAndPanier_Utilisateur_Id(Integer ligneId, Integer utilisateurId);

    void deleteByPanierId(Integer panierId);
}
