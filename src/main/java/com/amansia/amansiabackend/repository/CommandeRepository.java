package com.amansia.amansiabackend.repository;

import com.amansia.amansiabackend.entity.Commande;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommandeRepository extends JpaRepository<Commande, Integer> {
    boolean existsByAdresseLivraisonId(Integer adresseLivraisonId);

    List<Commande> findByUtilisateurIdOrderByDateCommandeDesc(Integer utilisateurId);

    Optional<Commande> findByNumeroCommande(String numeroCommande);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select commande from Commande commande where commande.id = :id")
    Optional<Commande> findByIdForUpdate(@Param("id") Integer id);
}
