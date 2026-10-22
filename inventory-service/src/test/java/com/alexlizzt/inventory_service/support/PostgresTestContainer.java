package com.alexlizzt.inventory_service.support;

import java.time.Duration;

import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Contenedor PostgreSQL compartido por toda la suite de pruebas.
 *
 * Se arranca manualmente una única vez por JVM (en lugar de delegar en la
 * extensión de Testcontainers) porque esta detiene el contenedor al finalizar
 * la primera clase de pruebas y deja sin base de datos a todas las siguientes.
 */
public final class PostgresTestContainer {

    private static final PostgreSQLContainer<?> INSTANCE = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("inventory_test_db")
            .withUsername("test_user")
            .withPassword("test_password")
            // initdb puede tardar más de 60 s en máquinas cargadas (CI que
            // compila imágenes o instala dependencias en paralelo).
            .withStartupTimeout(Duration.ofMinutes(3));

    static {
        INSTANCE.start();
    }

    private PostgresTestContainer() {
    }

    /**
     * Devuelve el contenedor compartido, arrancándolo si aún no está en marcha.
     */
    public static PostgreSQLContainer<?> instance() {
        return INSTANCE;
    }
}
