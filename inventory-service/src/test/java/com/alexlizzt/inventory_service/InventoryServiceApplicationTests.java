package com.alexlizzt.inventory_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import com.alexlizzt.inventory_service.support.PostgresTestContainer;

/**
 * Verifica que el contexto completo de la aplicación arranca con todas sus
 * dependencias cableadas. Se ejecuta sobre PostgreSQL real (Testcontainers),
 * por lo que también valida las migraciones de Flyway y el mapeo de entidades.
 */
@SpringBootTest(properties = {
        // URIs de ejemplo: el decodificador JWT se construye sin contactar Keycloak.
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:1/realms/test",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:1/realms/test/protocol/openid-connect/certs",
        // Evita que el indexador intente contactar el AI Service al arrancar.
        "app.semantic-search.reindex-on-startup=false"
})
class InventoryServiceApplicationTests {

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        PostgreSQLContainer<?> postgres = PostgresTestContainer.instance();
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void contextLoads() {
    }
}
