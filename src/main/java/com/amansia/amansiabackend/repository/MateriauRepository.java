package com.amansia.amansiabackend.repository;

import com.amansia.amansiabackend.entity.Materiau;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MateriauRepository extends JpaRepository<Materiau, Integer> {

    Optional<Materiau> findByNomIgnoreCase(String nom);

    boolean existsByNomIgnoreCase(String nom);

    List<Materiau> findByActifTrue();

    List<Materiau> findByNomContainingIgnoreCase(String nom);
}
