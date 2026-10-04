package com.amansia.amansiabackend.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class StockServiceIntegrationTest {
    @Autowired private StockService service;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void repeatedOrderOnlyConsumesStockOnce() {
        Integer varianteId = jdbc.queryForObject("SELECT variante_id FROM stocks WHERE quantite>0 ORDER BY variante_id LIMIT 1",Integer.class);
        Integer before = jdbc.queryForObject("SELECT quantite FROM stocks WHERE variante_id=?",Integer.class,varianteId);
        String reference = "TEST-" + UUID.randomUUID();
        service.vendre(varianteId,1,reference);
        service.vendre(varianteId,1,reference);
        assertEquals(before-1,jdbc.queryForObject("SELECT quantite FROM stocks WHERE variante_id=?",Integer.class,varianteId));
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM mouvements_stocks WHERE variante_id=? AND reference=?",Integer.class,varianteId,reference));
    }

    @Test
    void insufficientStockNeverGoesNegative() {
        Integer varianteId = jdbc.queryForObject("SELECT variante_id FROM stocks ORDER BY variante_id LIMIT 1",Integer.class);
        Integer before = jdbc.queryForObject("SELECT quantite FROM stocks WHERE variante_id=?",Integer.class,varianteId);
        assertThrows(IllegalArgumentException.class,() -> service.vendre(varianteId,before+1,"TEST-OVERSELL"));
        assertEquals(before,jdbc.queryForObject("SELECT quantite FROM stocks WHERE variante_id=?",Integer.class,varianteId));
    }
}
