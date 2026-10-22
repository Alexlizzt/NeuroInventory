package com.alexlizzt.inventory_service.infraestructure.persistence.springdata;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import com.alexlizzt.inventory_service.infraestructure.persistence.springdata.InventoryReportJpaRepository;

/**
 * Base de las pruebas de persistencia.
 *
 * El contenedor de PostgreSQL se arranca una única vez por JVM y permanece activo
 * durante toda la ejecución de la suite. Se gestiona de forma manual (en lugar de
 * delegar en la extensión de Testcontainers) para evitar que el contenedor se
 * detenga al finalizar la primera clase de pruebas y provoque fallos de conexión
 * en las clases siguientes.
 */
@DataJpaTest(
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = InventoryReportJpaRepository.class)
        }
)
@ActiveProfiles("test")
public abstract class AbstractJpaTest {

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("inventory_test_db")
            .withUsername("test_user")
            .withPassword("test_password");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
