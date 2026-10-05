package com.amansia.amansiabackend.repository;

import com.amansia.amansiabackend.entity.Notification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByUtilisateurIdOrderByDateCreationDescIdDesc(Integer utilisateurId);
    Optional<Notification> findByIdAndUtilisateurId(Integer id, Integer utilisateurId);
    long countByUtilisateurIdAndEstLueFalse(Integer utilisateurId);
    @Modifying
    @Query("update Notification n set n.estLue = true where n.utilisateur.id = :userId and n.estLue = false")
    int markAllRead(@Param("userId") Integer userId);
    @Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("delete from Notification n where n.utilisateur.id = :userId")
    int deleteForUser(@Param("userId") Integer userId);
}
