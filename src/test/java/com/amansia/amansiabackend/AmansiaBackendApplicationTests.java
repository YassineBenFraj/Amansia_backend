package com.amansia.amansiabackend;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.jpa.hibernate.ddl-auto=validate", "debug=false"})
class AmansiaBackendApplicationTests {
    @Autowired private Environment environment;

    @Test
    void contextLoads() throws Exception {
        assertEquals("validate", environment.getProperty("spring.jpa.hibernate.ddl-auto"));
        String base = "http://localhost:" + environment.getRequiredProperty("local.server.port");
        HttpClient client = HttpClient.newHttpClient();
        var publicResponse = client.send(HttpRequest.newBuilder(URI.create(base + "/api/categories/actives")).GET().build(),
                HttpResponse.BodyHandlers.discarding());
        assertEquals(200, publicResponse.statusCode());
        var adminResponse = client.send(HttpRequest.newBuilder(URI.create(base + "/api/commandes/admin")).GET().build(),
                HttpResponse.BodyHandlers.discarding());
        assertEquals(401, adminResponse.statusCode());
    }
}
