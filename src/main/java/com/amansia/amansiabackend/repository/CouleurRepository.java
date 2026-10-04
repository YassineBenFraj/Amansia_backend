package com.amansia.amansiabackend.repository;

import com.amansia.amansiabackend.entity.Couleur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouleurRepository extends JpaRepository<Couleur, Integer> {

    Optional<Couleur> findByNomIgnoreCase(String nom);

    boolean existsByNomIgnoreCase(String nom);

    List<Couleur> findByActifTrue();

    List<Couleur> findByNomContainingIgnoreCase(String nom);
}
