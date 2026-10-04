package com.amansia.amansiabackend.repository;

import com.amansia.amansiabackend.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Integer> {
    Optional<Stock> findByVarianteId(Integer varianteId);
}
