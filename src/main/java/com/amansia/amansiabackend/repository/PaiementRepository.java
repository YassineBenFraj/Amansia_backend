package com.amansia.amansiabackend.repository;

import com.amansia.amansiabackend.entity.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Integer> {
    @org.springframework.data.jpa.repository.Query("select p.commande.id from Paiement p where p.reference=:reference")
    Optional<Integer> findCommandeIdByReference(@org.springframework.data.repository.query.Param("reference") String reference);

    List<Paiement> findByCommandeId(Integer commandeId);

    List<Paiement> findByCommande_Utilisateur_IdOrderByDateCreationDesc(Integer utilisateurId);

    Optional<Paiement> findByIdAndCommande_Utilisateur_Id(Integer id, Integer utilisateurId);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select paiement from Paiement paiement where paiement.reference = :reference")
    Optional<Paiement> findByReferenceForUpdate(@org.springframework.data.repository.query.Param("reference") String reference);
}
