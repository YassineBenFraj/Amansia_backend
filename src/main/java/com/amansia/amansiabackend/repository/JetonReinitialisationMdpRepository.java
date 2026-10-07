package com.amansia.amansiabackend.repository;

import com.amansia.amansiabackend.entity.JetonReinitialisationMdp;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface JetonReinitialisationMdpRepository extends JpaRepository<JetonReinitialisationMdp, Long> {
    Optional<JetonReinitialisationMdp> findTopByUtilisateurIdOrderByDateCreationDesc(Integer utilisateurId);
    @Query("select j.utilisateur.id from JetonReinitialisationMdp j where j.tokenHash = :hash")
    Optional<Integer> findUtilisateurIdByTokenHash(@Param("hash") String hash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select j from JetonReinitialisationMdp j where j.tokenHash = :hash")
    Optional<JetonReinitialisationMdp> findByTokenHashForUpdate(@Param("hash") String hash);

    @Modifying
    @Query("update JetonReinitialisationMdp j set j.utilise = true where j.utilisateur.id = :utilisateurId and j.utilise = false")
    int invaliderJetonsActifs(@Param("utilisateurId") Integer utilisateurId);
}
