package com.amansia.amansiabackend.repository;

import com.amansia.amansiabackend.entity.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Integer> {

    List<Produit> findByCategorieId(Integer categorieId);

    List<Produit> findByActifTrue();

    List<Produit> findByCategorieIdAndActifTrue(Integer categorieId);

    List<Produit> findByNomContainingIgnoreCase(String nom);
}
