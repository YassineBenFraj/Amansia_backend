package com.amansia.amansiabackend.repository;

import com.amansia.amansiabackend.entity.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategorieRepository extends JpaRepository<Categorie, Integer> {

    Optional<Categorie> findByNomIgnoreCase(String nom);

    boolean existsByNomIgnoreCase(String nom);

    List<Categorie> findByActifTrue();
}
