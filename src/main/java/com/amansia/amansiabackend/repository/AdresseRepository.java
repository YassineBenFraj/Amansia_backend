package com.amansia.amansiabackend.repository;
import com.amansia.amansiabackend.entity.Adresse;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface AdresseRepository extends JpaRepository<Adresse,Integer> {
 List<Adresse> findByUtilisateurIdOrderByIdDesc(Integer utilisateurId);
 @Lock(LockModeType.PESSIMISTIC_WRITE)
 @Query("select a from Adresse a where a.id=:id and a.utilisateur.id=:utilisateurId")
 Optional<Adresse> findOwnedForUpdate(@Param("id") Integer id,@Param("utilisateurId") Integer utilisateurId);
}
