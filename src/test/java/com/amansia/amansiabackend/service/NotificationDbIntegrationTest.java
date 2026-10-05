package com.amansia.amansiabackend.service;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class NotificationDbIntegrationTest {
    @Autowired JdbcTemplate jdbc;

    @Test void repeatedBusinessEventCreatesOnlyOneNotification() {
        String event = "test:" + UUID.randomUUID();
        String sql = "INSERT INTO notifications(utilisateur_id,type,titre,message,est_lue,date_creation,evenement_cle) " +
                "VALUES(2147483647,'STOCK_FAIBLE','Test','Test',false,CURRENT_TIMESTAMP,?) " +
                "ON DUPLICATE KEY UPDATE id=id";
        jdbc.update(sql, event);
        jdbc.update(sql, event);
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM notifications WHERE utilisateur_id=2147483647 AND evenement_cle=?", Integer.class, event));
    }
}
