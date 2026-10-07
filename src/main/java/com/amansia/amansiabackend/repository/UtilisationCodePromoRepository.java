package com.amansia.amansiabackend.repository;

import com.amansia.amansiabackend.entity.UtilisationCodePromo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;

public interface UtilisationCodePromoRepository extends JpaRepository<UtilisationCodePromo, Integer> {
    long countByCodePromoIdAndUtilisateurId(Integer codePromoId, Integer utilisateurId);
    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select u from UtilisationCodePromo u where u.codePromo.id = :codeId and u.utilisateurId = :userId")
    List<UtilisationCodePromo> findForClientForUpdate(@Param("codeId") Integer codeId, @Param("userId") Integer userId);
    boolean existsByCommandeId(Integer commandeId);
}
