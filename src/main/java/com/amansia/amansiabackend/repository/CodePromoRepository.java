package com.amansia.amansiabackend.repository;

import com.amansia.amansiabackend.entity.CodePromo;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface CodePromoRepository extends JpaRepository<CodePromo, Integer> {
    Optional<CodePromo> findByCode(String code);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CodePromo c where c.code = :code")
    Optional<CodePromo> lockByCode(@Param("code") String code);
}
