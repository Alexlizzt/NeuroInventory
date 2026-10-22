package com.alexlizzt.inventory_service.infraestructure.persistence.springdata;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import com.alexlizzt.inventory_service.infraestructure.persistence.springdata.InventoryReportJpaRepository;
import com.alexlizzt.inventory_service.support.PostgresTestContainer;

/**
 * Base de las pruebas de persistencia.
 *
 * El contenedor de PostgreSQL lo gestiona {@link PostgresTestContainer}, que lo
 * arranca una única vez por JVM y lo mantiene activo durante toda la suite.
 */
@DataJpaTest(
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = InventoryReportJpaRepository.class)
        }
)
@ActiveProfiles("test")
public abstract class AbstractJpaTest {

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        PostgreSQLContainer<?> postgres = PostgresTestContainer.instance();
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
