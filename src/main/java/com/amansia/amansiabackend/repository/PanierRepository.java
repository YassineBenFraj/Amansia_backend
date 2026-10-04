package com.amansia.amansiabackend.repository;

import com.amansia.amansiabackend.entity.Panier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PanierRepository extends JpaRepository<Panier, Integer> {

    Optional<Panier> findByUtilisateurId(Integer utilisateurId);
}
