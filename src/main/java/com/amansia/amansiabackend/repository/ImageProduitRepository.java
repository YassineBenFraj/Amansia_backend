package com.amansia.amansiabackend.repository;

import com.amansia.amansiabackend.entity.ImageProduit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageProduitRepository extends JpaRepository<ImageProduit, Integer> {
    List<ImageProduit> findByUrl(String url);

    List<ImageProduit> findAllByOrderByOrdreAsc();

    List<ImageProduit> findByProduitIdOrderByOrdreAsc(Integer produitId);

    List<ImageProduit> findByProduitId(Integer produitId);
}
